package com.auction.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  private static DatabaseConnection instance;
  private Connection connection;

  private final String url = "jdbc:mysql://localhost:3306/auction_db";
  private final String user = "root";
  private final String pass = "Tuan792007";

  private DatabaseConnection() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Khởi tạo kết nối
      this.connection = DriverManager.getConnection(this.url, this.user, this.pass);

      if (this.connection != null) {
        System.out.println("✅ Kết nối Database thành công (Cổng 3306)!");
      }
    } catch (ClassNotFoundException e) {
      System.err.println("❌ Không tìm thấy Driver MySQL: " + e.getMessage());
    } catch (SQLException e) {
      System.err.println("❌ LỖI KẾT NỐI DATABASE: " + e.getMessage());
      System.err.println("👉 Hãy kiểm tra: 1. Đã bật MySQL chưa? 2. User/Pass đúng chưa? 3. Đã tạo DB 'auction_db' chưa?");
    }
  }

  public static synchronized DatabaseConnection getInstance() {
    if (instance == null) {
      instance = new DatabaseConnection();
    }
    return instance;
  }

  public Connection getConnection() {
    try {
      if (this.connection == null || this.connection.isClosed()) {
        instance = new DatabaseConnection();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return this.connection;
  }
}