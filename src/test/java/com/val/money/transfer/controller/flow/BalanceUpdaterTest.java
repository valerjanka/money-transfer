package com.val.money.transfer.controller.flow;

import com.val.money.transfer.controller.TransferInternalException;
import com.val.money.transfer.controller.TransferValidationException;
import com.val.money.transfer.dao.AccountDao;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.UserTransaction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.SQLException;

public class BalanceUpdaterTest {
    private AccountDao accountDaoMock;
    private Account firstAccount = new Account(1, 1, BigDecimal.TEN, "USD");
    private Account secondAccount = new Account(2, 2, BigDecimal.ONE, "USD");
    private Account anotherCurrencyAccount = new Account(3, 3, BigDecimal.TEN, "EUR");
    private UserTransaction firstToSecondTransaction = new UserTransaction(firstAccount.getId(), secondAccount.getId(),
            BigDecimal.ONE);
    private UserTransaction anotherCurrencyTransaction = new UserTransaction(firstAccount.getId(),
            anotherCurrencyAccount.getId(), BigDecimal.ONE);

    @Before
    public void setUp() {
        accountDaoMock = Mockito.mock(AccountDao.class);
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullDao() {
        new BalanceUpdater(null);
    }

    @Test(expected = NullPointerException.class)
    public void updateBalanceWithNullSource() throws TransferValidationException, TransferInternalException {
        new BalanceUpdater(accountDaoMock).updateBalances(null, secondAccount, firstToSecondTransaction);
    }

    @Test(expected = NullPointerException.class)
    public void updateBalanceWithNullDestination() throws TransferValidationException, TransferInternalException {
        new BalanceUpdater(accountDaoMock).updateBalances(firstAccount, null, firstToSecondTransaction);
    }

    @Test(expected = NullPointerException.class)
    public void updateBalanceWithNullTransaction() throws TransferValidationException, TransferInternalException {
        new BalanceUpdater(accountDaoMock).updateBalances(firstAccount, secondAccount, null);
    }

    @Test(expected = TransferValidationException.class)
    public void updateBalanceDifferentCurrency() throws TransferValidationException, TransferInternalException {
        new BalanceUpdater(accountDaoMock)
                .updateBalances(firstAccount, anotherCurrencyAccount, anotherCurrencyTransaction);
    }

    @Test(expected = TransferInternalException.class)
    public void updateBalanceDestinationWithWrongId() throws TransferValidationException, TransferInternalException {
        new BalanceUpdater(accountDaoMock)
                .updateBalances(firstAccount, anotherCurrencyAccount, firstToSecondTransaction);
    }

    @Test(expected = TransferInternalException.class)
    public void updateBalanceSourceWithWrongId() throws TransferValidationException, TransferInternalException {
        new BalanceUpdater(accountDaoMock)
                .updateBalances(anotherCurrencyAccount, secondAccount, firstToSecondTransaction);
    }

    @Test(expected = TransferValidationException.class)
    public void updateBalanceNotEnoughFundsOnSource() throws TransferValidationException, TransferInternalException {
        UserTransaction userTransaction = new UserTransaction(firstAccount.getId(), secondAccount.getId(),
                firstAccount.getBalance().add(BigDecimal.ONE));
        new BalanceUpdater(accountDaoMock)
                .updateBalances(firstAccount, secondAccount, userTransaction);
    }

    @Test(expected = TransferInternalException.class)
    public void updateBalanceSameAccount() throws TransferValidationException, TransferInternalException {
        UserTransaction userTransaction = new UserTransaction(firstAccount.getId(), firstAccount.getId(),
                firstAccount.getBalance());
        new BalanceUpdater(accountDaoMock)
                .updateBalances(firstAccount, firstAccount, userTransaction);
    }

    @Test
    public void updateBalanceSuccessfully() throws TransferValidationException, TransferInternalException, SQLException {
        UserTransaction userTransaction = new UserTransaction(firstAccount.getId(), secondAccount.getId(),
                BigDecimal.ONE);
        new BalanceUpdater(accountDaoMock)
                .updateBalances(firstAccount, secondAccount, userTransaction);
        Mockito.verify(accountDaoMock).updateBalance(firstAccount.getId(),
                firstAccount.getBalance().subtract(userTransaction.getTransferAmount()));
        Mockito.verify(accountDaoMock).updateBalance(secondAccount.getId(),
                secondAccount.getBalance().add(userTransaction.getTransferAmount()));
    }
}