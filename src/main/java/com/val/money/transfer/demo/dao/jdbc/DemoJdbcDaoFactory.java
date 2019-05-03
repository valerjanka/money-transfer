package com.val.money.transfer.demo.dao.jdbc;

import com.val.money.transfer.dao.jdbc.JdbcDaoFactory;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * JdbcDaoFactory with pre-populated demo model from file demo_model.sql
 */
public class DemoJdbcDaoFactory extends JdbcDaoFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String DEMO_FILE_NAME_KEY = "db.demo.population.file";
    private static final String DEMO_JDBC = "DEMO-JDBC";

    @Override
    public void init(Properties properties) {
        super.init(properties);
        String demoFileName = properties.getProperty(DEMO_FILE_NAME_KEY);
        Objects.requireNonNull(demoFileName, "Demo population file is not specified in properties with key '" +
                DEMO_FILE_NAME_KEY + "'");
        try {
            populateDemoModel(demoFileName);
        } catch (Exception e) {
            throw new IllegalStateException("Could not populate demo model: " + e.getMessage(), e);
        }
    }

    private void populateDemoModel(String demoFileName) throws SQLException, IOException {
        try(Connection connection = jdbcContext.getConnection()) {
            try(InputStream inputStream = DemoJdbcDaoFactory.class.getClassLoader().getResourceAsStream(demoFileName)) {
                if(inputStream == null) {
                    String message = "Could not find demo file '" + demoFileName + "' to populate DB";
                    LOGGER.error(message);
                    throw new FileNotFoundException(message);
                }
                try(ResultSet resultSet = RunScript.execute(connection, new InputStreamReader(inputStream))) {
                    connection.commit();
                } catch (SQLException e) {
                    LOGGER.error("Could not populate demo Model - ROLLBACK. Error : {}", e.getMessage(), e);
                    connection.rollback();
                    throw e;
                }
            } catch (IOException ex) {
                LOGGER.error("Got error during loading file '{}' to populate DB. Error: {}", demoFileName, ex.getMessage(), ex);
                throw ex;
            }
        } finally {
            jdbcContext.closeConnection();
        }
    }

    @Override
    public String getType() {
        return DEMO_JDBC;
    }
}
