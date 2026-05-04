package com.auction.server.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnection {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);

  private static final class Holder {
    static final DatabaseConnection INSTANCE = new DatabaseConnection();
  }

  private String url;
  private String user;
  private String password;

  private DatabaseConnection() {
    try {
      Properties props = new Properties();
      InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
      if (input != null) {
        props.load(input);
      } else {
        LOGGER.warn("db.properties not found on classpath, using defaults");
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/auction_db");
        props.setProperty("db.user", "ba_nin");
        props.setProperty("db.password", "banin123!");
      }

      this.url = props.getProperty("db.url");
      this.user = props.getProperty("db.user");
      this.password = props.getProperty("db.password");

      Class.forName("com.mysql.cj.jdbc.Driver");
      // Validate by opening a test connection
      Connection test = DriverManager.getConnection(url, user, password);
      test.close();
      LOGGER.info("Database connection validated successfully.");
    } catch (Exception e) {
      LOGGER.error("Failed to initialize database connection", e);
    }
  }

  public static DatabaseConnection getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Returns a NEW connection each call. Callers are responsible for closing it,
   * or using try-with-resources. This avoids sharing a single Connection across threads.
   */
  public Connection getConnection() {
    try {
      return DriverManager.getConnection(url, user, password);
    } catch (Exception e) {
      LOGGER.error("Failed to create database connection", e);
      return null;
    }
  }
}
