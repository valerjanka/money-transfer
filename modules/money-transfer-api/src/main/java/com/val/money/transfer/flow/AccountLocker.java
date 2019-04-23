package com.val.money.transfer.flow;

import com.val.money.transfer.dao.DaoConnection;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.Transaction;

public class AccountLocker {

    public AccountPair lockAndLoad(DaoConnection daoConnection, Transaction transaction) throws Exception {
        if (daoConnection == null) {
            throw new IllegalStateException("daoConnection can't be null");
        }
        if (transaction == null) {
            throw new IllegalArgumentException("transaction can't be null");
        }
        Account firstAccountToLock = getAndLockFirstAccount(daoConnection, transaction);
        Account secondAccountToLock = getAndLockSecondAccount(daoConnection, transaction);
        Account fromAccount = getFromAccount(transaction, firstAccountToLock, secondAccountToLock);
        Account toAccount = getToAccount(transaction, firstAccountToLock, secondAccountToLock);
        return new AccountPair(fromAccount, toAccount);
    }

    private Account getToAccount(Transaction transaction, Account firstAccountToLock, Account secondAccountToLock) {
        if (isFromAccountFirst(transaction)) {
            return secondAccountToLock;
        } else {
            return firstAccountToLock;
        }
    }

    private Account getFromAccount(Transaction transaction, Account firstAccountToLock, Account secondAccountToLock) {
        if (isFromAccountFirst(transaction)) {
            return firstAccountToLock;
        } else {
            return secondAccountToLock;
        }
    }

    private Account getAndLockSecondAccount(DaoConnection daoConnection, Transaction transaction) throws Exception {
        if (isFromAccountFirst(transaction)) {
            return daoConnection.getAndLockAccount(transaction.getToAccountNumber());
        } else {
            return daoConnection.getAndLockAccount(transaction.getFromAccountNumber());
        }
    }

    private Account getAndLockFirstAccount(DaoConnection daoConnection, Transaction transaction) throws Exception {
        if (isFromAccountFirst(transaction)) {
            return daoConnection.getAndLockAccount(transaction.getFromAccountNumber());
        } else {
            return daoConnection.getAndLockAccount(transaction.getToAccountNumber());
        }
    }

    private boolean isFromAccountFirst(Transaction transaction) {
        return transaction.getFromAccountNumber().compareToIgnoreCase(transaction.getToAccountNumber()) < 0;
    }

    public class AccountPair {
        private Account fromAccount;
        private Account toAccount;

        AccountPair(Account fromAccount, Account toAccount) {
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
        }

        public Account getFromAccount() {
            return fromAccount;
        }

        public Account getToAccount() {
            return toAccount;
        }
    }
}
