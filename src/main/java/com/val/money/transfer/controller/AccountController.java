package com.val.money.transfer.controller;

import com.val.money.transfer.TransferException;
import com.val.money.transfer.dao.DaoFactory;
import com.val.money.transfer.dao.DaoConnection;
import com.val.money.transfer.controller.flow.AccountLocker;
import com.val.money.transfer.controller.flow.AccountPair;
import com.val.money.transfer.controller.flow.BalanceUpdater;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.UserTransaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Controls main business logic and starts, commits and closes connections with DAO layer
 */
public class AccountController {
    private DaoFactory daoFactory;
    private AccountLocker accountLocker;
    private BalanceUpdater balanceUpdater;

    public List<Account> getAllAccounts() throws TransferException {
        try (DaoConnection daoConnection = daoFactory.getDaoConnection()) {
            if (daoConnection == null) {
                throw new IllegalStateException("daoFactory produces null daoConnection");
            }
            List<Account> result = daoFactory.getAccountDao().getAll();
            daoConnection.commit();
            return result;
        } catch (SQLException e) {
            throw new TransferInternalException("Database connection is failed: " + e.getMessage(), e);
        }
    }

    /**
     * Transfer funds from source account to destination account specified in user transaction. Validates
     * {@link UserTransaction} object and uses {@link AccountLocker}, {@link BalanceUpdater} to perform transaction.
     * Commit transaction if no errors occurred
     *
     * @param userTransaction with transfer data
     * @throws TransferException if any validation or internal exception occurs
     */
    public void transferFunds(UserTransaction userTransaction) throws TransferException {
        validateTransactionObject(userTransaction);
        try (DaoConnection daoConnection = daoFactory.getDaoConnection()) {
            if (daoConnection == null) {
                throw new IllegalStateException("daoFactory produces null daoConnection");
            }
            AccountPair accountPair = accountLocker.lockAndLoad(userTransaction);
            Account fromAccount = accountPair.getFromAccount();
            Account toAccount = accountPair.getToAccount();

            balanceUpdater.updateBalances(fromAccount, toAccount, userTransaction);
            daoConnection.commit();
        } catch (SQLException e) {
            throw new TransferInternalException("Database connection is failed: " + e.getMessage(), e);
        }
    }

    private void validateTransactionObject(UserTransaction userTransaction) throws TransferValidationException {
        if(userTransaction == null) {
            throw new IllegalArgumentException("userTransaction can't be null");
        }
        if(userTransaction.getTransferAmount() == null || userTransaction.getTransferAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferValidationException("Transfer amount is not positive value");
        }
        if(userTransaction.getFromAccountId() == userTransaction.getToAccountId()) {
            throw new TransferValidationException("Can't transfer funds to the same account, please specify different source and destination accounts");
        }
    }

    public void setDaoFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public void setAccountLocker(AccountLocker accountLocker) {
        this.accountLocker = accountLocker;
    }

    public void setBalanceUpdater(BalanceUpdater balanceUpdater) {
        this.balanceUpdater = balanceUpdater;
    }
}
