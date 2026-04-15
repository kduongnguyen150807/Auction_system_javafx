package com.auction.server.dao;

import com.auction.server.Factory.UserFactory;
import com.auction.server.Service.SQLService;
import com.auction.shared.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDao {
    private static UserDao instance;
    private final Connection connection;

    private UserDao() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND isactive = true";
        List<User> results =  SQLService.Fetch(sql, List.of(username, password), this.connection, this::mapUser);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean updateBalance(int id, double b){
        String sql = "UPDATE users SET BALANCE = ? WHERE id = ?";
        return SQLService.Update(sql, List.of(b, id), this.connection);
    }

    public boolean addBidderMetrics(int userId, double amount){
        String sql = "UPDATE users SET moneyspent = moneyspent  + ?, itembought = itembought + 1 WHERE id = ?";
        return SQLService.Update(sql, List.of(amount, userId), this.connection);
    }

    public boolean addSellerMetrics(int userId, double amount){
        String sql = "UPDATE users SET moneyreceived = moneyreceived + ?, itemssold = itemssold + 1 WHERE id = ?";;
        return SQLService.Update(sql, List.of(amount, userId), this.connection);
    }

    public User getById(String id){
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> results = SQLService.Fetch(sql, List.of(id), this.connection, this::mapUser);
        return results.isEmpty() ? null : results.get(0);
    }

    public User mapUser(ResultSet resultSet) {
        try {
            String role = resultSet.getString("role");
            User user = UserFactory.create(role);

            user.setId(resultSet.getInt("id"));
            user.setVersion(resultSet.getInt("version"));
            user.setUsername(resultSet.getString("username"));
            user.setFullName(resultSet.getString("fullname"));
            user.setEmail(resultSet.getString("email"));
            user.setPhoneNumber(resultSet.getString("phonenumber"));
            user.setBalance(resultSet.getDouble("balance"));
            user.setAvatarUrl(resultSet.getString("avatar_url"));
            user.setAvgRating(resultSet.getDouble("avgrating"));
            user.setTotalRatings(resultSet.getInt("totalratings"));
            user.setActive(resultSet.getBoolean("isactive"));
            user.setLocked(resultSet.getBoolean("islocked"));
            user.setMoneySpent(resultSet.getDouble("moneyspent"));
            user.setItemsBought(resultSet.getInt("itemsbought"));
            user.setMoneyReceived(resultSet.getDouble("moneyreceived"));
            user.setItemsSold(resultSet.getInt("itemssold"));
            return user;
        }catch (SQLException e){
            System.out.println("error setting attribute");
        }
        return null;
    }

    public static UserDao getInstance() {
        if (instance == null) {
            synchronized (UserDao.class) {
                if (instance == null) {
                    instance = new UserDao();
                }
            }
        }
        return instance;
    }
}