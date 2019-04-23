package com.val.money.transfer.h2;

import com.val.money.transfer.dao.DaoConnection;
import com.val.money.transfer.dao.DaoFactory;
import org.apache.commons.dbutils.DbUtils;

import java.sql.SQLException;
import java.util.Properties;

public class H2DaoFactory implements DaoFactory {
    public static final String TYPE = "H2";

    private static final String DRIVER_PROPERTY_NAME = "db.driver";
    private static final String CONNECTION_URL_PROPERTY_NAME = "db.connection.url";
    private static final String USER_PROPERTY_NAME = "db.user";
    private static final String PASSWORD_PROPERTY_NAME = "db.password";

    private String connectionUrl;
    private String user;
    private String password;

    @Override
    public void init(Properties properties) {
        System.out.println("init");
        if (!DbUtils.loadDriver(properties.getProperty(DRIVER_PROPERTY_NAME))) {
            throw new IllegalStateException("H2 Driver was't found. Check property with name '" +
                    DRIVER_PROPERTY_NAME + "'");
        }
        connectionUrl = properties.getProperty(CONNECTION_URL_PROPERTY_NAME);
        user = properties.getProperty(USER_PROPERTY_NAME);
        password = properties.getProperty(PASSWORD_PROPERTY_NAME);
    }

    @Override
    public DaoConnection createTransferDaoConnection() throws SQLException {
        System.out.println("createTransferDaoConnection");
        return new H2DaoConnection(connectionUrl, user, password);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
