package com.auction.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  private static DatabaseConnection instance;
  private final String url = "jdbc:mysql://localhost:3306/nhandatabase";
  private final String user = "nhan_nguyen";
  private final String pass = "nhancoi21";

  private DatabaseConnection() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      System.err.println("MySQL Driver not found: " + e.getMessage());
    }
  }

  public static synchronized DatabaseConnection getInstance() {
    if (instance == null) {
      instance = new DatabaseConnection();
    }
    return instance;
  }

  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(this.url, this.user, this.pass);
  }
}