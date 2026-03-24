package com.auction.server.dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private String url = "jdbc:mysql://localhost:3306/auction_db";
    private String user = "root";
    private String pass = "khanhmoc1236";
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(this.url, this.user, this.pass);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static DatabaseConnection getinstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        DatabaseConnection ans = instance;
        return ans;
    }
    public Connection getconnection() {
        Connection ans = this.connection;
        return ans;
    }
}