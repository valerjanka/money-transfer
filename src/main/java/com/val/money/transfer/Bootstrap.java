package com.val.money.transfer;

import com.val.money.transfer.controller.AccountController;
import com.val.money.transfer.dao.AccountDao;
import com.val.money.transfer.dao.DaoFactory;
import com.val.money.transfer.controller.flow.AccountLocker;
import com.val.money.transfer.controller.flow.BalanceUpdater;
import com.val.money.transfer.rest.RestServer;
import com.val.money.transfer.rest.TransferExceptionMapper;
import com.val.money.transfer.rest.TransferService;
import com.val.money.transfer.util.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Main class to start Money Transfer REST API.
 * Rest Server and DAO factory is configurable in {@value PROPERTIES_FILE_NAME}, so they could be changed
 */
public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PROPERTIES_FILE_NAME = "app.properties";
    private static final String REST_SERVER_PROPERTY_NAME = "rest.server.type";
    private static final String DAO_FACTORY_PROPERTY_NAME = "dao.factory.type";

    public static void main(String[] args) throws Exception {
        Properties properties = loadProperties();
        DaoFactory daoFactory = initDb(properties);
        AccountController accountController = initController(daoFactory);
        startServer(properties, accountController);
    }

    private static AccountController initController(DaoFactory daoFactory) {
        AccountDao accountDao = daoFactory.getAccountDao();
        AccountController accountController = new AccountController();
        accountController.setAccountLocker(new AccountLocker(accountDao));
        accountController.setBalanceUpdater(new BalanceUpdater(accountDao));
        accountController.setDaoFactory(daoFactory);
        return accountController;
    }

    private static Properties loadProperties() throws IOException {
        LOGGER.info("Loading properties from '{}'", PROPERTIES_FILE_NAME);
        return PropertiesLoader.load(PROPERTIES_FILE_NAME);
    }

    private static void startServer(Properties properties, AccountController accountController) throws Exception {
        String type = properties.getProperty(REST_SERVER_PROPERTY_NAME);
        RestServer restServer = MoneyTransferServiceLoader.loadRestServer(type);
        LOGGER.info("Initializing REST server");
        TransferService transferService = new TransferService();
        transferService.setAccountController(accountController);
        List restServices = Arrays.asList(transferService, new TransferExceptionMapper());
        restServer.init(properties, restServices);
        LOGGER.info("Starting REST server");
        try {
            restServer.start();
            restServer.join();
        } finally {
            restServer.stop();
        }
    }

    private static DaoFactory initDb(Properties properties) {
        LOGGER.info("Initializing DaoFactory");
        String type = properties.getProperty(DAO_FACTORY_PROPERTY_NAME);
        DaoFactory daoFactory = MoneyTransferServiceLoader.loadDaoFactory(type);
        daoFactory.init(properties);
        return daoFactory;
    }
}
