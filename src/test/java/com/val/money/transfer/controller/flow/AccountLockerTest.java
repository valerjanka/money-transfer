package com.val.money.transfer.controller.flow;

import com.val.money.transfer.controller.TransferInternalException;
import com.val.money.transfer.dao.AccountDao;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.UserTransaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.SQLException;

public class AccountLockerTest {
    private AccountDao accountDaoMock;

    @Before
    public void setUp() {
        accountDaoMock = Mockito.mock(AccountDao.class);

    }

    @Test(expected = NullPointerException.class)
    public void lockAndLoadWithNullDao() {
        new AccountLocker(null);
    }

    @Test(expected = NullPointerException.class)
    public void lockAndLoadWithNullTransaction() throws TransferInternalException {
        AccountLocker accountLocker = new AccountLocker(accountDaoMock);
        accountLocker.lockAndLoad(null);
    }

    @Test(expected = TransferInternalException.class)
    public void lockAndLoadDaoThrowsSqlExceptionForSource() throws TransferInternalException, SQLException {
        AccountLocker accountLocker = new AccountLocker(accountDaoMock);
        UserTransaction userTransaction = new UserTransaction(1, 2, BigDecimal.ONE);
        Mockito.doThrow(new SQLException()).when(accountDaoMock).lockForUpdateAccount(1);
        accountLocker.lockAndLoad(userTransaction);
    }

    @Test(expected = TransferInternalException.class)
    public void lockAndLoadDaoThrowsSqlExceptionForDestination() throws TransferInternalException, SQLException {
        AccountLocker accountLocker = new AccountLocker(accountDaoMock);
        UserTransaction userTransaction = new UserTransaction(1, 2, BigDecimal.ONE);
        Mockito.doThrow(new SQLException()).when(accountDaoMock).lockForUpdateAccount(2);
        accountLocker.lockAndLoad(userTransaction);
    }

    @Test
    public void lockAndLoadSuccessfullyInOrder() throws TransferInternalException, SQLException {
        AccountLocker accountLocker = new AccountLocker(accountDaoMock);
        UserTransaction userTransaction = new UserTransaction(1, 2, BigDecimal.ONE);
        Account from = new Account(1, 1, BigDecimal.TEN, "USD");
        Account to = new Account(2, 2, BigDecimal.TEN, "USD");
        Mockito.doReturn(from).when(accountDaoMock).lockForUpdateAccount(from.getId());
        Mockito.doReturn(to).when(accountDaoMock).lockForUpdateAccount(to.getId());
        AccountPair pair = accountLocker.lockAndLoad(userTransaction);
        Assert.assertNotNull(pair);
        Assert.assertEquals(from, pair.getFromAccount());
        Assert.assertEquals(to, pair.getToAccount());
        InOrder orderVerifier = Mockito.inOrder(accountDaoMock);
        orderVerifier.verify(accountDaoMock).lockForUpdateAccount(1);
        orderVerifier.verify(accountDaoMock).lockForUpdateAccount(2);
    }

    @Test
    public void lockAndLoadSuccessfullyInReverseOrder() throws TransferInternalException, SQLException {
        AccountLocker accountLocker = new AccountLocker(accountDaoMock);
        UserTransaction userTransaction = new UserTransaction(2, 1, BigDecimal.ONE);
        Account from = new Account(2, 1, BigDecimal.TEN, "USD");
        Account to = new Account(1, 2, BigDecimal.TEN, "USD");
        Mockito.doReturn(from).when(accountDaoMock).lockForUpdateAccount(from.getId());
        Mockito.doReturn(to).when(accountDaoMock).lockForUpdateAccount(to.getId());
        AccountPair pair = accountLocker.lockAndLoad(userTransaction);
        Assert.assertNotNull(pair);
        Assert.assertEquals(from, pair.getFromAccount());
        Assert.assertEquals(to, pair.getToAccount());
        InOrder orderVerifier = Mockito.inOrder(accountDaoMock);
        orderVerifier.verify(accountDaoMock).lockForUpdateAccount(1);
        orderVerifier.verify(accountDaoMock).lockForUpdateAccount(2);
    }
}