<<<<<<< HEAD
<<<<<<< HEAD
package com.auction.server.dao;
import java.sql.Connection;
import java.sql.DriverManager;
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection conn;
    private String url = "jdbc:mysql://localhost:3306/auction_db";
    private String user = "root";
    private String pass = "khanhmoc1236";
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conn = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
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
        Connection ans = this.conn;
        return ans;
    }
}
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
