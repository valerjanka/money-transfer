package com.val.money.transfer.rest;

import com.val.money.transfer.ServiceTypeHolder;

import java.util.Properties;

public interface RestServer extends ServiceTypeHolder {
    String PORT_PROPERTY_NAME = "rest.server.port";

    void init(Properties properties, TransferService transferService);

    void start() throws Exception;
}
