package com.val.money.transfer;

import com.val.money.transfer.dao.DaoFactory;
import com.val.money.transfer.dao.DaoConnection;
import com.val.money.transfer.flow.AccountLocker;
import com.val.money.transfer.flow.BalanceUpdater;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.Transaction;

import java.math.BigDecimal;

public class TransferController {
    private DaoFactory daoFactory;
    private AccountLocker accountLocker;
    private BalanceUpdater balanceUpdater;

    public void transferFunds(Transaction transaction) throws Exception {
        validateTransactionObject(transaction);
        try (DaoConnection daoConnection = daoFactory.createTransferDaoConnection()) {
            if (daoConnection == null) {
                throw new IllegalStateException("daoFactory produces null daoConnection");
            }
            AccountLocker.AccountPair accountPair = accountLocker.lockAndLoad(daoConnection, transaction);
            Account fromAccount = accountPair.getFromAccount();
            Account toAccount = accountPair.getToAccount();

            balanceUpdater.updateBalances(daoConnection, fromAccount, toAccount, transaction);
            daoConnection.commit();
        }
    }

    private void validateTransactionObject(Transaction transaction) throws TransferException {
        if(transaction == null) {
            throw new IllegalArgumentException("transaction can't be null");
        }
        if(transaction.getFromAccountNumber() == null) {
            throw new IllegalArgumentException("transaction fromAccount property should not be null");
        }
        if(transaction.getToAccountNumber() == null) {
            throw new IllegalArgumentException("transaction toAccount property should not be null");
        }
        if(transaction.getTransferAmount() == null || transaction.getTransferAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferException("Transfer amount is not positive value");
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
