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
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);

  private static final class Holder {
    static final DatabaseConnection INSTANCE = new DatabaseConnection();
  }

  private HikariDataSource dataSource;

  private DatabaseConnection() {
    try {
      Properties props = new Properties();
      InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
      if (input != null) {
        props.load(input);
      } else {
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/auction_db");
        props.setProperty("db.user", "ba_nin");
        props.setProperty("db.password", "banin123");
      }

      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(props.getProperty("db.url"));
      config.setUsername(props.getProperty("db.user"));
      config.setPassword(props.getProperty("db.password"));
      config.setDriverClassName("com.mysql.cj.jdbc.Driver");

      // Cấu hình Pool chống sập Server
      config.setMaximumPoolSize(50);
      config.setMinimumIdle(10);
      config.setConnectionTimeout(30000);

      this.dataSource = new HikariDataSource(config);
      LOGGER.info("HikariCP Database connection pool initialized.");
    } catch (Exception e) {
      LOGGER.error("Failed to initialize database connection", e);
    }
  }

  public static DatabaseConnection getInstance() {
    return Holder.INSTANCE;
  }

  public Connection getConnection() {
    try {
      return dataSource.getConnection(); // Lấy từ Pool, cực nhanh và an toàn
    } catch (SQLException e) {
      LOGGER.error("Failed to get connection from pool", e);
      return null;
    }
  }
}