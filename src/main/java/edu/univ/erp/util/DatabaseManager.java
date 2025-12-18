package edu.univ.erp.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {

    private static DatabaseManager instance;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private HikariDataSource authDataSource;
    private HikariDataSource erpDataSource;
    private Properties properties;

    private DatabaseManager(){

        loadProperties();
        initializeDataSources();
    }

    public static synchronized DatabaseManager getInstance(){

        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadProperties(){
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")){

            if (input == null) {
                logger.warn("Unable to find application.properties, using defaults");
                setDefaultProperties();
                return;
            }
            properties.load(input);
        }
        catch (IOException e) {
            logger.error("Error loading application.properties", e);
            setDefaultProperties();
        }
    }

    private void setDefaultProperties(){

        properties.setProperty("auth.db.url", "jdbc:mysql://localhost:3306/auth_db");
        properties.setProperty("auth.db.username", "root");
        properties.setProperty("auth.db.password", "");
        properties.setProperty("erp.db.url", "jdbc:mysql://localhost:3306/erp_db");
        properties.setProperty("erp.db.username", "root");
        properties.setProperty("erp.db.password", "");
        properties.setProperty("db.pool.maximumPoolSize", "100");
        properties.setProperty("db.pool.minimumIdle", "15");
        properties.setProperty("db.pool.connectionTimeout", "30000");
    }

    private void initializeDataSources(){

        HikariConfig authConfig = new HikariConfig();

        authConfig.setJdbcUrl(properties.getProperty("auth.db.url"));
        authConfig.setUsername(properties.getProperty("auth.db.username"));
        authConfig.setPassword(properties.getProperty("auth.db.password"));
        authConfig.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.maximumPoolSize", "100")));
        authConfig.setMinimumIdle(Integer.parseInt(properties.getProperty("db.pool.minimumIdle", "15")));
        authConfig.setConnectionTimeout(Long.parseLong(properties.getProperty("db.pool.connectionTimeout", "30000")));
        authConfig.setPoolName("Auth-DB-Pool");

        authDataSource = new HikariDataSource(authConfig);

        HikariConfig erpConfig = new HikariConfig();

        erpConfig.setJdbcUrl(properties.getProperty("erp.db.url"));
        erpConfig.setUsername(properties.getProperty("erp.db.username"));
        erpConfig.setPassword(properties.getProperty("erp.db.password"));
        erpConfig.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.maximumPoolSize", "100")));
        erpConfig.setMinimumIdle(Integer.parseInt(properties.getProperty("db.pool.minimumIdle", "15")));
        erpConfig.setConnectionTimeout(Long.parseLong(properties.getProperty("db.pool.connectionTimeout", "30000")));
        erpConfig.setPoolName("Erp-DB-Pool");

        erpDataSource = new HikariDataSource(erpConfig);

        logger.info("Database connection pools initialized successfully");
    }

    public Connection getAuthConnection() throws SQLException {
        return authDataSource.getConnection();
    }

    public Connection getErpConnection() throws SQLException {
        return erpDataSource.getConnection();
    }

    public void close() {

        if (authDataSource != null && !authDataSource.isClosed()) {
            authDataSource.close();
            logger.info("Auth database connection pool closed");
        }
        if (erpDataSource != null && !erpDataSource.isClosed()) {
            erpDataSource.close();
            logger.info("ERP database connection pool closed");
        }
    }

}
