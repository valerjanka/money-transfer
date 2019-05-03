package com.val.money.transfer;

import com.val.money.transfer.dao.DaoFactory;
import com.val.money.transfer.rest.RestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ServiceLoader;

/**
 *  Loads registered services by class, which extends {@link ServiceTypeHolder} and string type
 */
public class MoneyTransferServiceLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static RestServer loadRestServer(String type) {
        return load(type, RestServer.class);
    }

    public static DaoFactory loadDaoFactory(String type) {
        return load(type, DaoFactory.class);
    }

    private static <K extends ServiceTypeHolder> K load(String type, Class<K> clazz) {
        if (type == null) {
            throw new IllegalArgumentException(clazz.getCanonicalName() + " type can't be null");
        }
        LOGGER.debug("Loading '{}' implementation from classpath for type '{}'", clazz.getCanonicalName(), type);
        ServiceLoader<K> loader = ServiceLoader.load(clazz);
        for (K service : loader) {
            if (type.equals(service.getType())) {
                LOGGER.info("Service '{}' is loaded for type '{}'", service.getClass().getCanonicalName(), type);
                return service;
            }
        }
        throw new IllegalStateException("No '" + clazz.getCanonicalName() + "' service found with type '" + type + "'");
    }
}
