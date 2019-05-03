package com.val.money.transfer.rest;

import com.val.money.transfer.controller.AccountController;
import com.val.money.transfer.controller.TransferValidationException;
import com.val.money.transfer.controller.flow.AccountLocker;
import com.val.money.transfer.controller.flow.BalanceUpdater;
import com.val.money.transfer.dao.AccountDao;
import com.val.money.transfer.dao.DaoFactory;
import com.val.money.transfer.demo.dao.jdbc.DemoJdbcDaoFactory;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.UserTransaction;
import com.val.money.transfer.util.PropertiesLoader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Integration test: Business logic + H2 DB
 */
public class TransferServiceITest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PROPERTIES_FILE_NAME = "test.properties";
    private static final int BALANCE_IN_ACCOUNT1 = 1000;
    private static final int BALANCE_IN_ACCOUNT3 = 1000;
    private static final int TRANSFER_AMOUNT = 10;

    private final UserTransaction USER_TRANSACTION =
            new UserTransaction(1, 3, BigDecimal.ONE);
    private final AtomicInteger FAILED_COUNTER = new AtomicInteger(0);

    private TransferService transferService;

    @Before
    public void setUp() throws IOException {
        transferService = initService();
        FAILED_COUNTER.set(0);
    }

    @Test
    public void getAll() throws TransferValidationException {
        List<Account> result = transferService.getAll();
        Assert.assertNotNull(result);
        Assert.assertEquals(5, transferService.getAll().size());
    }

    @Test
    public void transferConsecutive() throws TransferValidationException {
        int balance = BALANCE_IN_ACCOUNT1;
        int transferAmount = USER_TRANSACTION.getTransferAmount().intValue();
        while (balance >= TRANSFER_AMOUNT) {
            transferService.transfer(USER_TRANSACTION);
            balance -= transferAmount;
        }
        checkAfterTransferAllFromFirstToThird(balance);
    }

    /**
     * Transfer balance = 1000 from 1st account to 3rd account with balance 1000 in 1000 tasks,
     * which are executed in a thread pool with size 100. Transfer amount = 1 USD
     *
     * @throws Exception
     */
    @Test
    public void transferInParallel1000Tasks() throws Exception {
        int amountOfThreads = 100;
        int amountOfTasks = 1000;
        transferInParallel(amountOfThreads, amountOfTasks);
        Assert.assertEquals(0, FAILED_COUNTER.get());
        checkAfterTransferAllFromFirstToThird(0);
    }

    /**
     * Transfer balance = 1000 from 1st account to 3rd account with balance 1000 in 1500 tasks,
     * which are executed in a thread pool with size 100. Transfer amount = 1 USD. 500 tasks must fail
     *
     * @throws Exception
     */
    @Test
    public void transferInParallel1500TasksWithFailure() throws Exception {
        ExecutorService executorService = null;
        int amountOfThreads = 100;
        int amountOfTasks = 1500;
        transferInParallel(amountOfThreads, amountOfTasks);
        Assert.assertEquals(500, FAILED_COUNTER.get());
        checkAfterTransferAllFromFirstToThird(0);
    }

    private void transferInParallel(int amountOfThreads, int amountOfTasks) {
        ExecutorService executorService = null;
        try {
            executorService = Executors.newFixedThreadPool(amountOfThreads);
            List<TransferFundTask> tasks = createTasks(amountOfTasks);
            Queue<Future<?>> futures = new LinkedList<>();
            for (TransferFundTask task : tasks) {
                futures.offer(executorService.submit(task));
            }
            while (!futures.isEmpty()) {
                try {
                    futures.poll().get();
                } catch (Exception e) {
                    LOGGER.error("Failed during waiting for Future - might be ok. Error: {}", e.getMessage(), e);
                }
            }
        } finally {
            LOGGER.info("Failed: '{}' from '{}' tasks", FAILED_COUNTER.get(), 1000);
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
        }
    }

    private List<TransferFundTask> createTasks(int amountOfThreads) {
        return Stream.generate(TransferFundTask::new).limit(amountOfThreads).collect(Collectors.toList());
    }

    private void checkAfterTransferAllFromFirstToThird(int balance) throws TransferValidationException {
        List<Account> accounts = transferService.getAll();
        Account firstAccount = getAccount(accounts, 1);
        Account thirdAccount = getAccount(accounts, 3);
        BigDecimal expectedOnFirst = BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedOnThird = BigDecimal.valueOf(BALANCE_IN_ACCOUNT3 + BALANCE_IN_ACCOUNT1 - balance)
                .setScale(2, RoundingMode.HALF_EVEN);
        Assert.assertEquals(expectedOnFirst, firstAccount.getBalance());
        Assert.assertEquals(expectedOnThird, thirdAccount.getBalance());
    }

    @After
    public void tearDown() {

    }

    private Account getAccount(List<Account> accounts, int id) {
        return accounts.stream().filter((a) -> a.getId() == id).findFirst().get();
    }

    private TransferService initService() throws IOException {
        DaoFactory daoFactory = initDb();
        AccountController accountController = initController(daoFactory);
        TransferService transferService = new TransferService();
        transferService.setAccountController(accountController);
        return transferService;
    }

    private AccountController initController(DaoFactory daoFactory) {
        AccountDao accountDao = daoFactory.getAccountDao();
        AccountController accountController = new AccountController();
        accountController.setAccountLocker(new AccountLocker(accountDao));
        accountController.setBalanceUpdater(new BalanceUpdater(accountDao));
        accountController.setDaoFactory(daoFactory);
        return accountController;
    }

    private static DaoFactory initDb() throws IOException {
        LOGGER.info("Loading properties from '{}'", PROPERTIES_FILE_NAME);
        Properties properties = PropertiesLoader.load(PROPERTIES_FILE_NAME);
        LOGGER.info("Initializing DaoFactory");
        DaoFactory daoFactory = new DemoJdbcDaoFactory();
        daoFactory.init(properties);
        return daoFactory;
    }

    class TransferFundTask implements Runnable {
        @Override
        public void run() {
            try {
                transferService.transfer(USER_TRANSACTION);
            } catch (TransferValidationException e) {
                FAILED_COUNTER.incrementAndGet();
                LOGGER.error("Failed executing transfer fund task with id {}. Error: {}", Thread.currentThread().getId(), e.getMessage(), e);
            }
        }
    }
}