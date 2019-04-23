package com.val.money.transfer.dao;

import com.val.money.transfer.ServiceTypeHolder;

import java.sql.SQLException;
import java.util.Properties;

public interface DaoFactory extends ServiceTypeHolder {
    void init(Properties properties);

    DaoConnection createTransferDaoConnection() throws SQLException;
}
