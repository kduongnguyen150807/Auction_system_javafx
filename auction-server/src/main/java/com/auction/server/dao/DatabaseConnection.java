package com.auction.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  private static DatabaseConnection instance;
  private Connection connection;
  private String url = "jdbc:mysql://localhost:3306/nhandatabase";
  private String user = "nhan_nguyen";
  private String pass = "nhancoi21";

  private DatabaseConnection() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      this.connection = DriverManager.getConnection(this.url, this.user, this.pass);
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
    }
  }

  public static DatabaseConnection getInstance() {
    if (instance == null) {
      instance = new DatabaseConnection();
    }
    DatabaseConnection ans = instance;
    return ans;
  }

  public Connection getConnection() {
    Connection ans = this.connection;
    return ans;
  }
}
