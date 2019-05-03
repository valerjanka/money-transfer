package com.val.money.transfer.dao.jdbc;

import com.val.money.transfer.dao.DaoConnection;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.*;

/**
 * Wrapper on JDBC connection. Uses {@link JdbcContext} to create/get connection. Execute rollback on close
 * if connection wasn't committed yet
 */
public class JdbcDaoConnection implements DaoConnection {
    private final Connection connection;
    private final JdbcContext context;
    private boolean committed;

    public JdbcDaoConnection(JdbcContext jdbcContext) throws SQLException {
        context = jdbcContext;
        connection = jdbcContext.getConnection();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
        committed = true;
    }

    @Override
    public void close() throws SQLException {
        if(!committed) {
            connection.rollback();
        }
        context.closeConnection();
    }
}
