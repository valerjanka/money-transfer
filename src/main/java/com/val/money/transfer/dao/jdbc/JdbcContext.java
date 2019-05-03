package com.val.money.transfer.dao.jdbc;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Holds JDBC connections for each thread. Creates them if not exist and closes them by request
 */
public class JdbcContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final ThreadLocal<Connection> CONNECTION_THREAD_LOCAL = new ThreadLocal<>();
    private String connectionUrl;
    private String user;
    private String password;

    public JdbcContext(String connectionUrl, String user, String password) {
        this.connectionUrl = connectionUrl;
        this.user = user;
        this.password = password;
    }

    /**
     * Get existed for such thread connection or creates a new one
     *
     * @return existed or new connection
     * @throws SQLException exception during JDBC connection creation
     */
    public Connection getConnection() throws SQLException {
        Connection connection = CONNECTION_THREAD_LOCAL.get();
        if(connection == null) {
            connection = createConnection();
            CONNECTION_THREAD_LOCAL.set(connection);
        }
        return connection;
    }

    public void closeConnection() {
        Connection connection = CONNECTION_THREAD_LOCAL.get();
        if(connection ==  null) {
            throw new IllegalStateException("No connection to close for current thread");
        }
        CONNECTION_THREAD_LOCAL.remove();
        DbUtils.closeQuietly(connection);
    }

    private Connection createConnection() throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(connectionUrl, user, password);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            LOGGER.error("Can't create jdbc connection with connectionUrl '{}' and user '{}'", connectionUrl, user);
            if(connection != null) {
                DbUtils.closeQuietly(connection);
            }
            throw e;
        }
        return connection;
    }

}
