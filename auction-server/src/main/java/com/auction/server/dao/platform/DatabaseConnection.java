package com.auction.server.dao.platform;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnection {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

  private static final int MAX_POOL_SIZE = 50;
  private static final int MIN_IDLE_CONNECTIONS = 10;
  private static final long CONNECTION_TIMEOUT_MS = 30000;

  private static final String DB_PROPERTIES_FILE = "db.properties";
  private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

  private static final class Holder {
    static final DatabaseConnection instance = new DatabaseConnection();
  }

  private HikariDataSource datasource;

  private DatabaseConnection() {
    try {
      Properties props = loadDatabaseProperties();

      String dbUrl = getConfigValue("DB_URL", props, "db.url");
      String dbUser = getConfigValue("DB_USER", props, "db.user");
      String dbPassword = getConfigValue("DB_PASS", props, "db.password");

      validateDatabaseConfig(dbUrl, dbUser, dbPassword);

      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      config.setUsername(dbUser);
      config.setPassword(dbPassword);
      config.setDriverClassName(MYSQL_DRIVER);
      config.setMaximumPoolSize(MAX_POOL_SIZE);
      config.setMinimumIdle(MIN_IDLE_CONNECTIONS);
      config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);

      this.datasource = new HikariDataSource(config);
      logger.info("Database connection pool initialized successfully.");
    } catch (Exception e) {
      logger.error("db_init_error", e);
      throw new IllegalStateException("Cannot initialize database connection pool", e);
    }
  }

  private Properties loadDatabaseProperties() {
    Properties props = new Properties();

    try (InputStream input =
                 getClass().getClassLoader().getResourceAsStream(DB_PROPERTIES_FILE)) {

      if (input != null) {
        props.load(input);
      } else {
        logger.warn("{} not found, environment variables will be used instead.", DB_PROPERTIES_FILE);
      }
    } catch (Exception e) {
      logger.warn("Failed to load {}", DB_PROPERTIES_FILE, e);
    }

    return props;
  }

  private String getConfigValue(String envKey, Properties props, String propertyKey) {
    String envValue = System.getenv(envKey);
    if (envValue != null && !envValue.isBlank()) {
      return envValue;
    }

    return props.getProperty(propertyKey);
  }

  private void validateDatabaseConfig(String dbUrl, String dbUser, String dbPassword) {
    if (dbUrl == null || dbUrl.isBlank()) {
      throw new IllegalStateException("Missing database URL. Please set DB_URL or db.url.");
    }

    if (dbUser == null || dbUser.isBlank()) {
      throw new IllegalStateException("Missing database user. Please set DB_USER or db.user.");
    }

    if (dbPassword == null) {
      throw new IllegalStateException("Missing database password. Please set DB_PASS or db.password.");
    }
  }

  public static DatabaseConnection getInstance() {
    return Holder.instance;
  }

  public Connection getConnection() {
    if (datasource == null) {
      throw new IllegalStateException("Database datasource has not been initialized.");
    }

    try {
      return datasource.getConnection();
    } catch (SQLException e) {
      logger.error("pool_error", e);
      throw new IllegalStateException("Cannot get database connection from pool", e);
    }
  }

  public void closePool() {
    if (datasource != null && !datasource.isClosed()) {
      datasource.close();
      logger.info("Database connection pool closed safely.");
    }
  }
}