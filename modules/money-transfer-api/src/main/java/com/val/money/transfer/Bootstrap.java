package com.val.money.transfer;

import com.val.money.transfer.dao.DaoFactory;
import com.val.money.transfer.flow.AccountLocker;
import com.val.money.transfer.flow.BalanceUpdater;
import com.val.money.transfer.rest.RestServer;
import com.val.money.transfer.rest.TransferService;
import com.val.money.transfer.util.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PROPERTIES_FILE_NAME = "app.properties";
    private static final String REST_SERVER_PROPERTY_NAME = "rest.server.type";
    private static final String DAO_FACTORY_PROPERTY_NAME = "dao.factory.type";

    public static void main(String[] args) throws Exception {
        Properties properties = loadProperties();
        DaoFactory daoFactory = initDb(properties);
        TransferController transferController = initController(daoFactory);
        startServer(properties, transferController);
    }

    private static TransferController initController(DaoFactory daoFactory) {
        TransferController transferController = new TransferController();
        transferController.setAccountLocker(new AccountLocker());
        transferController.setBalanceUpdater(new BalanceUpdater());
        transferController.setDaoFactory(daoFactory);
        return transferController;
    }

    private static Properties loadProperties() throws IOException {
        LOGGER.info("Loading properties from '{}'", PROPERTIES_FILE_NAME);
        return PropertiesLoader.load(PROPERTIES_FILE_NAME);
    }

    private static void startServer(Properties properties, TransferController transferController) throws Exception {
        String type = properties.getProperty(REST_SERVER_PROPERTY_NAME);
        RestServer restServer = MoneyTransferServiceLoader.loadRestServer(type);
        LOGGER.info("Initializing REST server");
        TransferService transferService = new TransferService();
        transferService.setTransferController(transferController);
        restServer.init(properties, transferService);
        LOGGER.info("Starting REST server");
        restServer.start();
    }

    private static DaoFactory initDb(Properties properties) {
        LOGGER.info("Initializing DaoFactory");
        String type = properties.getProperty(DAO_FACTORY_PROPERTY_NAME);
        DaoFactory daoFactory = MoneyTransferServiceLoader.loadDaoFactory(type);
        daoFactory.init(properties);
        return daoFactory;
    }
}
