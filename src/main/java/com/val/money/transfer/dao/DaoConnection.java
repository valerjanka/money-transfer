package com.val.money.transfer.dao;

import java.sql.SQLException;

/**
 * Initializes connection, creates transaction if needed
 */
public interface DaoConnection extends AutoCloseable {

    void commit() throws SQLException;

    /**
     * Finalizes the flow via unlocking accounts, rollback transaction if it wasn't committed, closing connection
     */
    void close() throws SQLException;
}
