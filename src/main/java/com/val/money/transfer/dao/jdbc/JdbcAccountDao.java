package com.val.money.transfer.dao.jdbc;

import com.val.money.transfer.dao.AccountDao;
import com.val.money.transfer.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic JDBC implementation of Account DAO. Should operate with already opened {@link Connection} and does not
 * close or rollback it.
 */
public class JdbcAccountDao implements AccountDao {
    private final static String SQL_GET_ACCOUNT_BY_NUMBER = "SELECT * FROM account WHERE id = ? FOR UPDATE";
    private final static String SQL_UPDATE_ACCOUNT_BALANCE = "UPDATE account SET balance = ? WHERE id = ? ";
    private final static String SQL_GET_ALL_ACCOUNTS = "SELECT * FROM account";

    private JdbcContext context;

    public JdbcAccountDao(JdbcContext context) {
        this.context = context;
    }

    @Override
    public List<Account> getAll() throws SQLException {
        Connection connection = context.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_ALL_ACCOUNTS)) {
            try (ResultSet rs = statement.executeQuery()) {
                List<Account> result = new LinkedList<>();
                while (rs.next()) {
                    result.add(new Account(rs.getLong("id"), rs.getLong("userId"),
                            rs.getBigDecimal("balance"), rs.getString("currency")));
                }
                return result;
            }
        }
    }

    @Override
    public Account lockForUpdateAccount(long accountId) throws SQLException {
        Connection connection = context.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_ACCOUNT_BY_NUMBER)) {
            statement.setLong(1, accountId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Account(rs.getLong("id"), rs.getLong("userId"),
                            rs.getBigDecimal("balance"), rs.getString("currency"));
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public int updateBalance(long accountId, BigDecimal newBalance) throws SQLException {
        Connection connection = context.getConnection();
        try(PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_ACCOUNT_BALANCE)) {
            statement.setBigDecimal(1, newBalance);
            statement.setLong(2, accountId);
            return statement.executeUpdate();
        }
    }

}
