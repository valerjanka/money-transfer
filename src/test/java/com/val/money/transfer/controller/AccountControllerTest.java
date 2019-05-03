package com.val.money.transfer.controller;

import com.val.money.transfer.TransferException;
import com.val.money.transfer.controller.flow.AccountLocker;
import com.val.money.transfer.controller.flow.AccountPair;
import com.val.money.transfer.controller.flow.BalanceUpdater;
import com.val.money.transfer.dao.AccountDao;
import com.val.money.transfer.dao.DaoConnection;
import com.val.money.transfer.dao.DaoFactory;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.UserTransaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;

public class AccountControllerTest {
    private DaoFactory daoFactoryMock;
    private AccountDao accountDaoMock;
    private AccountLocker accountLockerMock;
    private BalanceUpdater balanceUpdaterMock;
    private DaoConnection daoConnectionMock;
    private static List<Account> accounts = Arrays.asList(new Account(1, 1, BigDecimal.ONE, "USD"),
            new Account(2, 2, BigDecimal.TEN, "USD"));

    @Before
    public void setUp() throws SQLException {
        daoFactoryMock = Mockito.mock(DaoFactory.class);
        accountDaoMock = Mockito.mock(AccountDao.class);
        Mockito.doReturn(accountDaoMock).when(daoFactoryMock).getAccountDao();
        accountLockerMock = Mockito.mock(AccountLocker.class);
        balanceUpdaterMock = Mockito.mock(BalanceUpdater.class);
        daoConnectionMock = Mockito.mock(DaoConnection.class);
        Mockito.doReturn(daoConnectionMock).when(daoFactoryMock).getDaoConnection();
    }

    @Test
    public void getAllAccountsSuccessfully() throws TransferException, SQLException {
        Mockito.doReturn(accounts).when(accountDaoMock).getAll();
        AccountController accountController = createAccountControllerWithMocks();
        List<Account> result = accountController.getAllAccounts();
        Assert.assertEquals(accounts, result);
        Mockito.verify(daoConnectionMock, times(1)).commit();
        Mockito.verify(daoConnectionMock, times(1)).close();
    }

    @Test(expected = Exception.class)
    public void getAllAccountsConnectionFailedCheckRollback() throws TransferException, SQLException {
        Mockito.doThrow(new SQLException()).when(accountDaoMock).getAll();
        AccountController accountController = createAccountControllerWithMocks();
        try {
            accountController.getAllAccounts();
        } finally {
            Mockito.verify(daoConnectionMock, times(0)).commit();
            Mockito.verify(daoConnectionMock, times(1)).close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void transferFundsNullUserTransaction() throws TransferException, SQLException {
        AccountController accountController = createAccountControllerWithMocks();
        try {
            accountController.transferFunds(null);
        } finally {
            Mockito.verify(daoFactoryMock, times(0)).getDaoConnection();
        }
    }

    @Test(expected = TransferValidationException.class)
    public void transferFundsEmptyUserTransaction() throws TransferException, SQLException {
        AccountController accountController = createAccountControllerWithMocks();
        try {
            accountController.transferFunds(new UserTransaction());
        } finally {
            Mockito.verify(daoFactoryMock, times(0)).getDaoConnection();
        }
    }

    @Test(expected = TransferValidationException.class)
    public void transferFundsZeroAmount() throws TransferException, SQLException {
        AccountController accountController = createAccountControllerWithMocks();
        try {
            accountController.transferFunds(new UserTransaction(1, 2, BigDecimal.ZERO));
        } finally {
            Mockito.verify(daoFactoryMock, times(0)).getDaoConnection();
        }
    }

    @Test(expected = TransferValidationException.class)
    public void transferFundsNegativeAmount() throws TransferException, SQLException {
        AccountController accountController = createAccountControllerWithMocks();
        try {
            accountController.transferFunds(new UserTransaction(1, 2, BigDecimal.valueOf(-1)));
        } finally {
            Mockito.verify(daoFactoryMock, times(0)).getDaoConnection();
        }
    }

    @Test(expected = TransferValidationException.class)
    public void transferFundsOnSameAccount() throws TransferException, SQLException {
        AccountController accountController = createAccountControllerWithMocks();
        try {
            accountController.transferFunds(new UserTransaction(1, 1, BigDecimal.ONE));
        } finally {
            Mockito.verify(daoFactoryMock, times(0)).getDaoConnection();
        }
    }

    @Test(expected = TransferInternalException.class)
    public void transferFundsAccountLockerThrowsHandledException() throws TransferException, SQLException {
        AccountController accountController = createAccountControllerWithMocks();
        UserTransaction userTransaction = new UserTransaction(1, 2, BigDecimal.ONE);
        Mockito.doThrow(new TransferInternalException()).when(accountLockerMock).lockAndLoad(userTransaction);
        try {
            accountController.transferFunds(userTransaction);
        } finally {
            Mockito.verify(daoFactoryMock, times(1)).getDaoConnection();
            Mockito.verify(daoConnectionMock, times(1)).close();
        }
    }

    @Test(expected = RuntimeException.class)
    public void transferFundsAccountLockerThrowsRuntimeException() throws TransferException, SQLException {
        AccountController accountController = createAccountControllerWithMocks();
        UserTransaction userTransaction = new UserTransaction(1, 2, BigDecimal.ONE);
        Mockito.doThrow(new RuntimeException()).when(accountLockerMock).lockAndLoad(userTransaction);
        try {
            accountController.transferFunds(userTransaction);
        } finally {
            Mockito.verify(daoFactoryMock, times(1)).getDaoConnection();
            Mockito.verify(daoConnectionMock, times(1)).close();
        }
    }

    @Test(expected = TransferInternalException.class)
    public void transferFundsBalanceUpdaterThrowsInternalException() throws TransferException, SQLException {
        AccountController accountController = createAccountControllerWithMocks();
        Account accountFrom = new Account(1, 1, BigDecimal.TEN, "USD");
        Account accountTo = new Account(2, 1, BigDecimal.TEN, "USD");
        AccountPair pair = new AccountPair(accountFrom, accountTo);
        UserTransaction userTransaction = new UserTransaction(1, 2, BigDecimal.ONE);
        Mockito.doReturn(pair).when(accountLockerMock).lockAndLoad(userTransaction);
        Mockito.doThrow(new TransferInternalException()).when(balanceUpdaterMock).updateBalances(accountFrom, accountTo, userTransaction);

        try {
            accountController.transferFunds(userTransaction);
        } finally {
            Mockito.verify(daoFactoryMock, times(1)).getDaoConnection();
            Mockito.verify(daoConnectionMock, times(1)).close();
        }
    }

    @Test(expected = RuntimeException.class)
    public void transferFundsBalanceUpdaterThrowsRuntimeException() throws TransferException, SQLException {
        AccountController accountController = createAccountControllerWithMocks();
        Account accountFrom = new Account(1, 1, BigDecimal.TEN, "USD");
        Account accountTo = new Account(2, 1, BigDecimal.TEN, "USD");
        AccountPair pair = new AccountPair(accountFrom, accountTo);
        UserTransaction userTransaction = new UserTransaction(1, 2, BigDecimal.ONE);
        Mockito.doReturn(pair).when(accountLockerMock).lockAndLoad(userTransaction);
        Mockito.doThrow(new RuntimeException()).when(balanceUpdaterMock).updateBalances(accountFrom, accountTo, userTransaction);

        try {
            accountController.transferFunds(userTransaction);
        } finally {
            Mockito.verify(daoFactoryMock, times(1)).getDaoConnection();
            Mockito.verify(daoConnectionMock, times(1)).close();
        }
    }

    @Test
    public void transferFundsSuccessfully() throws TransferException {
        AccountController accountController = createAccountControllerWithMocks();
        Account accountFrom = new Account(1, 1, BigDecimal.TEN, "USD");
        Account accountTo = new Account(2, 1, BigDecimal.TEN, "USD");
        AccountPair pair = new AccountPair(accountFrom, accountTo);
        UserTransaction userTransaction = new UserTransaction(1, 2, BigDecimal.ONE);
        Mockito.doReturn(pair).when(accountLockerMock).lockAndLoad(userTransaction);

        accountController.transferFunds(userTransaction);
        Mockito.verify(balanceUpdaterMock, times(1)).updateBalances(accountFrom, accountTo, userTransaction);
    }


    private AccountController createAccountControllerWithMocks() {
        AccountController accountController = new AccountController();
        accountController.setAccountLocker(accountLockerMock);
        accountController.setBalanceUpdater(balanceUpdaterMock);
        accountController.setDaoFactory(daoFactoryMock);
        return accountController;
    }
}