package com.val.money.transfer.rest;

import com.val.money.transfer.ServiceTypeHolder;

import java.util.List;
import java.util.Properties;

/**
 * Interface for REST server, which could be implemented and registered as a service
 */
public interface RestServer extends ServiceTypeHolder {
    String PORT_PROPERTY_NAME = "rest.server.port";

    void init(Properties properties, List restServices);

    void start() throws Exception;

    void join() throws Exception;

    void stop() throws Exception;
}
