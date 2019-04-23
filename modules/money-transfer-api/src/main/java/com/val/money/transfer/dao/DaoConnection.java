package com.val.money.transfer.dao;

import com.val.money.transfer.TransferException;
import com.val.money.transfer.model.Account;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Initializes connection, creates transaction if needed
 */
public interface DaoConnection extends AutoCloseable {

    Account getAndLockAccount(String accountNumber) throws Exception;

    void updateBalance(String accountNumber, BigDecimal newBalance) throws Exception;

    void commit() throws Exception;

    /**
     * Finalizes the flow via unlocking accounts, committing/rollback transaction, closing connection
     */
    void close() throws Exception;
}
