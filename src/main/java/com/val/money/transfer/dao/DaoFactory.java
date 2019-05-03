package com.val.money.transfer.dao;

import com.val.money.transfer.ServiceTypeHolder;

import java.sql.SQLException;
import java.util.Properties;

/**
 * DAO Factory, which should be registered as Service and would be loaded by type.
 */
public interface DaoFactory extends ServiceTypeHolder {
    void init(Properties properties);
    DaoConnection getDaoConnection() throws SQLException;
    AccountDao getAccountDao();
}
