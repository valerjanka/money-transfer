package com.val.money.transfer.h2;

import com.val.money.transfer.TransferException;
import com.val.money.transfer.dao.DaoConnection;
import com.val.money.transfer.model.Account;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class H2DaoConnection implements DaoConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final static String SQL_GET_ACCOUNT_BY_NUMBER = "SELECT * FROM account WHERE accountNumber = ? FOR UPDATE";
    private final static String SQL_UPDATE_ACCOUNT_BALANCE = "UPDATE account SET balance = ? WHERE accountNumber = ? ";

    private final Connection connection;
    private List<PreparedStatement> preparedStatements = new LinkedList<>();
    private List<ResultSet> resultSetList = new LinkedList<>();
    private boolean isCommitted = false;

    public H2DaoConnection(String connectionUrl, String user, String password) throws SQLException {
        connection = DriverManager.getConnection(connectionUrl, user, password);
        connection.setAutoCommit(false);
    }

    @Override
    public Account getAndLockAccount(String accountNumber) throws TransferException, SQLException {
        // get and lock the account
        PreparedStatement statement = connection.prepareStatement(SQL_GET_ACCOUNT_BY_NUMBER);
        preparedStatements.add(statement);
        statement.setString(1, accountNumber);
        ResultSet rs = statement.executeQuery();
        resultSetList.add(rs);
        if (rs.next()) {
            return new Account(rs.getString("accountNumber"), rs.getLong("userId"),
                    rs.getBigDecimal("balance"), rs.getString("currency"));
        } else {
            throw new TransferException("No account found in DB with accountNumber = '" + accountNumber + "'");
        }
    }

    @Override
    public void updateBalance(String accountNumber, BigDecimal newBalance) throws SQLException, TransferException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_ACCOUNT_BALANCE);
        preparedStatements.add(statement);
        statement.setBigDecimal(1, newBalance);
        statement.setString(2, accountNumber);
        int updated = statement.executeUpdate();
        if (updated != 1) {
            throw new TransferException("Could not update Account balance by accountNumber correctly. " +
                    "Returned amount of updated rows: " + updated);
        }
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
        isCommitted = true;
    }

    @Override
    public void close() throws SQLException {
        try {
            if (!isCommitted) {
                if (connection != null) {
                    LOGGER.info("Executing rollback as transaction wasn't successfully finished or committed.");
                    connection.rollback();
                }
            }
        } finally {
            for (ResultSet rs : resultSetList) {
                DbUtils.closeQuietly(rs);
            }
            for (PreparedStatement ps : preparedStatements) {
                DbUtils.closeQuietly(ps);
            }
            DbUtils.closeQuietly(connection);
        }
    }
}
