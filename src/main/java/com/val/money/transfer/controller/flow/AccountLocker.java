package com.val.money.transfer.controller.flow;

import com.val.money.transfer.controller.TransferInternalException;
import com.val.money.transfer.dao.AccountDao;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.UserTransaction;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Loads accounts for user transaction. <br>
 *     Before load, it locks accounts in order of their id to do not have db deadlocks.
 *     But all services should have same rule
 */
public class AccountLocker {
    private AccountDao accountDao;

    public AccountLocker(AccountDao accountDao) {
        Objects.requireNonNull(accountDao, "accountDao can't be null");
        this.accountDao = accountDao;
    }

    /**
     * Lock and load Accounts for future update. <br>
     * To minimize amount of deadlocks, accounts are locked in id order
     * Method does not handle transaction state and requires transaction to be started before calling this method
     *
     * @param userTransaction user transfer funds transaction
     * @return source and destination accounts
     * @throws TransferInternalException if any internal error occurs, like executing sql statement
     */
    public AccountPair lockAndLoad(UserTransaction userTransaction) throws TransferInternalException {
        Objects.requireNonNull(userTransaction, "userTransaction can't be null");
        return getLockedAccountsInIdOrder(userTransaction);
    }

    private AccountPair getLockedAccountsInIdOrder(UserTransaction userTransaction) throws TransferInternalException {
        Account firstAccountToLock = getAndLockFirstAccount(userTransaction);
        Account secondAccountToLock = getAndLockSecondAccount(userTransaction);
        return createFromToPair(userTransaction, firstAccountToLock, secondAccountToLock);
    }

    private AccountPair createFromToPair(UserTransaction userTransaction, Account firstAccountToLock, Account secondAccountToLock) {
        if (isFirstFromAccount(userTransaction)) {
            return new AccountPair(firstAccountToLock, secondAccountToLock);
        } else {
            return new AccountPair(secondAccountToLock, firstAccountToLock);
        }
    }

    private Account getAndLockSecondAccount(UserTransaction userTransaction) throws TransferInternalException {
        if (isFirstFromAccount(userTransaction)) {
            try {
                return accountDao.lockForUpdateAccount(userTransaction.getToAccountId());
            } catch (SQLException e) {
                throw new TransferInternalException("Could not get destination account by id '" + userTransaction.getToAccountId()
                        + "'", e);
            }
        } else {
            try {
                return accountDao.lockForUpdateAccount(userTransaction.getFromAccountId());
            } catch (SQLException e) {
                throw new TransferInternalException("Could not get source account by id '" + userTransaction.getFromAccountId()
                        + "'", e);
            }
        }
    }

    private Account getAndLockFirstAccount(UserTransaction userTransaction) throws TransferInternalException {
        if (isFirstFromAccount(userTransaction)) {
            try {
                return accountDao.lockForUpdateAccount(userTransaction.getFromAccountId());
            } catch (SQLException e) {
                throw new TransferInternalException("Could not get source account by id '" + userTransaction.getFromAccountId()
                        + "'", e);
            }
        } else {
            try {
                return accountDao.lockForUpdateAccount(userTransaction.getToAccountId());
            } catch (SQLException e) {
                throw new TransferInternalException("Could not get destination account by id '" + userTransaction.getToAccountId()
                        + "'", e);
            }
        }
    }

    private boolean isFirstFromAccount(UserTransaction userTransaction) {
        return userTransaction.getFromAccountId() - userTransaction.getToAccountId() < 0;
    }

}
