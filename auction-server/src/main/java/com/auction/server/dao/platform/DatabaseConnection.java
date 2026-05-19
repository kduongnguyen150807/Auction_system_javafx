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

  private static final class Holder {
    static final DatabaseConnection instance = new DatabaseConnection();
  }

  private HikariDataSource datasource;

  private DatabaseConnection() {
    try {
      Properties props = new Properties();
      InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
      if (input != null) {
        props.load(input);
      }
      String dburl = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : props.getProperty("db.url");
      String dbuser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : props.getProperty("db.user");
      String dbpass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : props.getProperty("db.password");
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dburl);
      config.setUsername(dbuser);
      config.setPassword(dbpass);
      config.setDriverClassName("com.mysql.cj.jdbc.Driver");
      config.setMaximumPoolSize(50);
      config.setMinimumIdle(10);
      config.setConnectionTimeout(30000);
      this.datasource = new HikariDataSource(config);
    } catch (Exception e) {
      logger.error("db_init_error", e);
    }
  }

  public static DatabaseConnection getInstance() {
    return Holder.instance;
  }

  public Connection getConnection() {
    try {
      return datasource.getConnection();
    } catch (SQLException e) {
      logger.error("pool_error", e);
      return null;
    }
  }

  // TÍNH NĂNG 4: HÀM ĐÓNG POOL KHI SHUTDOWN SERVER
  public void closePool() {
    if (datasource != null && !datasource.isClosed()) {
      datasource.close();
      logger.info("Đã đóng HikariCP Database Connection Pool an toàn.");
    }
  }
}