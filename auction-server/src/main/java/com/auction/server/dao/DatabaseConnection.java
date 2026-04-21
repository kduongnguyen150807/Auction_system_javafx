package com.auction.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConnection {
  private static DatabaseConnection instance;
  private final HikariDataSource dataSource;

  private String url = "jdbc:mysql://localhost:3306/auction_db";
  private String user = "root";
  private String pass = "Tuan792007";

  private DatabaseConnection(){
    HikariConfig config = new HikariConfig();

    // essentials
    config.setJdbcUrl(url);
    config.setUsername(user);
    config.setPassword(pass);

    // pool config cho dự án
    config.setMaximumPoolSize(50);
    config.setMinimumIdle(10);
    config.setConnectionTimeout(30000);
    config.setIdleTimeout(600000);
    config.setMaxLifetime(1800000);

    this.dataSource = new HikariDataSource(config);
  }
  public static synchronized DatabaseConnection getInstance() {
    if (instance == null) {
      instance = new DatabaseConnection();
    }
    DatabaseConnection ans = instance;
    return ans;
  }

  public void closePool() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
    }
  }

  public Connection getConnection() throws SQLException{
    Connection ans = dataSource.getConnection();
    return ans;
  }
}
