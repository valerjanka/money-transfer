package com.val.money.transfer.flow;

import com.val.money.transfer.TransferException;
import com.val.money.transfer.dao.DaoConnection;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.Transaction;

import java.math.BigDecimal;

public class BalanceUpdater {
    /**
     * Check and transfer funds from source account to destination account.
     * <p>Validates that accounts have the same currency and source account balance </p>
     *
     * @param daoConnection to update balance on data source.
     * @param fromAccount   source account
     * @param toAccount     destination account
     * @param transaction   incoming transaction
     * @throws TransferException if currency is different or source account does not have enough funds
     */
    public void updateBalances(DaoConnection daoConnection, Account fromAccount, Account toAccount,
                               Transaction transaction) throws Exception {
        if (daoConnection == null || fromAccount == null || toAccount == null || transaction == null) {
            throw new IllegalArgumentException("Arguments can't be null");
        }
        validateCurrency(fromAccount, toAccount);
        validateBalance(fromAccount, transaction);
        BigDecimal newFromAccountBalance = calculateNewFromAccountBalance(fromAccount, transaction);
        BigDecimal newToAccountBalance = calculateNewToAccountBalance(fromAccount, transaction);
        daoConnection.updateBalance(fromAccount.getAccountNumber(), newFromAccountBalance);
        daoConnection.updateBalance(toAccount.getAccountNumber(), newToAccountBalance);
    }

    private BigDecimal calculateNewToAccountBalance(Account fromAccount, Transaction transaction) {
        return fromAccount.getBalance().add(transaction.getTransferAmount());
    }

    private BigDecimal calculateNewFromAccountBalance(Account fromAccount, Transaction transaction) {
        return fromAccount.getBalance().subtract(transaction.getTransferAmount());
    }

    /**
     * Account currency should be the same and not null.
     *
     * @param fromAccount from which funds would be transferred
     * @param toAccount   to which funds would be transferred
     */
    private void validateCurrency(Account fromAccount, Account toAccount) throws TransferException {
        if (fromAccount.getCurrency() == null || toAccount.getCurrency() == null) {
            throw new IllegalStateException("Account currency can't be null");
        }
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new TransferException("Currencies of from account (" + fromAccount.getCurrency() +
                    ") and to account (" + toAccount.getCurrency() + ") are different");
        }
    }

    /**
     * Check whether fromAccount has enough money for transfer.
     */
    private void validateBalance(Account fromAccount, Transaction transaction) throws TransferException {
        if (transaction.getTransferAmount() == null) {
            throw new IllegalStateException("Transfer amount could not be null");
        }
        if (transaction.getTransferAmount().compareTo(fromAccount.getBalance()) > 0) {
            throw new TransferException("Sorce account '" + fromAccount.getAccountNumber() + "' does not have enough fund");
        }
    }
}
