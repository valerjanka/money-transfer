package com.val.money.transfer.dao;

import com.val.money.transfer.model.Account;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Simple DAO for Account object with limited functionality
 */
public interface AccountDao {
    List<Account> getAll() throws SQLException;
    Account lockForUpdateAccount(long id) throws SQLException;
    int updateBalance(long id, BigDecimal newBalance) throws SQLException;
}
