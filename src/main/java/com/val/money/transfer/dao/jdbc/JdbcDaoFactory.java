package com.val.money.transfer.dao.jdbc;

import com.val.money.transfer.dao.AccountDao;
import com.val.money.transfer.dao.DaoConnection;
import com.val.money.transfer.dao.DaoFactory;
import org.apache.commons.dbutils.DbUtils;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * JDBC DAO Factory implementation from properties file. Creates JDBC {@link AccountDao} and {@link DaoConnection}
 */
public class JdbcDaoFactory implements DaoFactory {
    private static final String TYPE = "JDBC";

    private static final String DRIVER_PROPERTY_NAME = "db.driver";
    private static final String CONNECTION_URL_PROPERTY_NAME = "db.connection.url";
    private static final String USER_PROPERTY_NAME = "db.user";
    private static final String PASSWORD_PROPERTY_NAME = "db.password";

    protected JdbcContext jdbcContext;
    private JdbcAccountDao accountDao;

    @Override
    public void init(Properties properties) {
        String dbDrive = properties.getProperty(DRIVER_PROPERTY_NAME);
        Objects.requireNonNull(dbDrive, "JDBC driver is not specified in properties file. Ensure, that property '"
                + DRIVER_PROPERTY_NAME + "' is specified");
        if (!DbUtils.loadDriver(properties.getProperty(DRIVER_PROPERTY_NAME))) {
            throw new IllegalStateException("JDBC driver was't found. Property name '" +
                    DRIVER_PROPERTY_NAME + "' with value = '" + dbDrive + "'");
        }
        String connectionUrl = properties.getProperty(CONNECTION_URL_PROPERTY_NAME);
        String user = properties.getProperty(USER_PROPERTY_NAME);
        String password = properties.getProperty(PASSWORD_PROPERTY_NAME);

        jdbcContext = new JdbcContext(connectionUrl, user, password);
        accountDao = new JdbcAccountDao(jdbcContext);
        populateDemoData();
    }

    private void populateDemoData() {
    }

    @Override
    public DaoConnection getDaoConnection() throws SQLException {
        return new JdbcDaoConnection(jdbcContext);
    }

    @Override
    public AccountDao getAccountDao() {
        return accountDao;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
