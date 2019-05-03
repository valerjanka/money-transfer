package com.val.money.transfer.controller.flow;

import com.val.money.transfer.controller.TransferInternalException;
import com.val.money.transfer.controller.TransferValidationException;
import com.val.money.transfer.dao.AccountDao;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.UserTransaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Update balance for source and destination accounts with amount specified in user transaction <br>
 * Validates source account balance and check that both accounts have same currency. No conversion is supported
 */
public class BalanceUpdater {
    private AccountDao accountDao;

    public BalanceUpdater(AccountDao accountDao) {
        Objects.requireNonNull(accountDao, "accountDao can't be null");
        this.accountDao = accountDao;
    }

    /**
     * Check and transfer funds from source account to destination account.
     * <p>Validates that accounts have the same currency and source account balance </p>
     * Method does not handle transaction state and requires transaction to be started before calling this method
     *
     * @param fromAccount     source account
     * @param toAccount       destination account
     * @param userTransaction incoming userTransaction
     * @throws TransferValidationException if currency is different or source account does not have enough funds
     */
    public void updateBalances(Account fromAccount, Account toAccount,
                               UserTransaction userTransaction) throws TransferValidationException,
            TransferInternalException {
        Objects.requireNonNull(fromAccount);
        Objects.requireNonNull(toAccount);
        Objects.requireNonNull(userTransaction);

        validateAccountIds(fromAccount, toAccount, userTransaction);
        validateCurrency(fromAccount, toAccount);
        validateBalance(fromAccount, userTransaction);
        BigDecimal newFromAccountBalance = calculateNewFromAccountBalance(fromAccount, userTransaction);
        BigDecimal newToAccountBalance = calculateNewToAccountBalance(toAccount, userTransaction);
        try {
            accountDao.updateBalance(fromAccount.getId(), newFromAccountBalance);
        } catch (SQLException e) {
            throw new TransferInternalException("Could not update balance of source account '" + fromAccount.getId() +
                    "'", e);
        }

        try {
            accountDao.updateBalance(toAccount.getId(), newToAccountBalance);
        } catch (SQLException e) {
            throw new TransferInternalException("Could not update balance of destination account '" + toAccount.getId()
                    + "'", e);
        }
    }

    private void validateAccountIds(Account fromAccount, Account toAccount, UserTransaction userTransaction)
            throws TransferInternalException {
        if (fromAccount.getId() != userTransaction.getFromAccountId()) {
            throw new TransferInternalException("Provided source account id '" + fromAccount.getId() +
                    "'is different then in user transaction '" + userTransaction.getFromAccountId() + "'");
        }
        if (toAccount.getId() != userTransaction.getToAccountId()) {
            throw new TransferInternalException("Provided destination account id '" + fromAccount.getId() +
                    "'is different then in user transaction '" + userTransaction.getToAccountId() + "'");
        }
        if(fromAccount.getId() == toAccount.getId()) {
            throw new TransferInternalException("Provided source and destination accounts have same id '" +
                    fromAccount.getId() + "'");
        }
    }

    private BigDecimal calculateNewToAccountBalance(Account fromAccount, UserTransaction userTransaction) {
        return fromAccount.getBalance().add(userTransaction.getTransferAmount());
    }

    private BigDecimal calculateNewFromAccountBalance(Account fromAccount, UserTransaction userTransaction) {
        return fromAccount.getBalance().subtract(userTransaction.getTransferAmount());
    }

    /**
     * Account currency should be the same and not null.
     *
     * @param fromAccount from which funds would be transferred
     * @param toAccount   to which funds would be transferred
     */
    private void validateCurrency(Account fromAccount, Account toAccount) throws TransferValidationException {
        Objects.requireNonNull(fromAccount.getCurrency(), "Account currency can't be null");
        Objects.requireNonNull(toAccount.getCurrency(), "Account currency can't be null");
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new TransferValidationException("Currencies of from account (" + fromAccount.getCurrency() +
                    ") and to account (" + toAccount.getCurrency() + ") are different");
        }
    }

    /**
     * Check whether fromAccount has enough money for transfer.
     */
    private void validateBalance(Account fromAccount, UserTransaction userTransaction)
            throws TransferValidationException {
        Objects.requireNonNull(userTransaction.getTransferAmount(), "Transfer amount could not be null");
        if (userTransaction.getTransferAmount().compareTo(fromAccount.getBalance()) > 0) {
            throw new TransferValidationException("Source account '" + fromAccount.getId() +
                    "' does not have enough fund");
        }
    }
}
