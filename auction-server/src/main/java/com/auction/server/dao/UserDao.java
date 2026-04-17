package com.auction.server.dao;

import com.auction.shared.UserFactory;
import com.auction.server.Service.SQLService;
import com.auction.shared.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao extends BaseDao{
    private static UserDao instance;
    private UserDao() {}

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND isactive = true";
        List<User> results =  executeFetch(sql, List.of(username, password), this::mapUser);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean signup(User u){
        String profileName = normalize(u.getFullName());
        if (profileName.isBlank()) profileName = normalize(u.getUsername());
        String sql = "INSERT INTO users (username, fullname, password, email, age, phonenumber, role, isactive, islocked) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            return executeUpdate(sql, List.of(
                    normalize(u.getUsername()),
                    profileName,
                    u.getPassword(),
                    normalize(u.getEmail()),
                    u.getAge(),
                    normalize(u.getPhoneNumber()),
                    normalize(u.getRole().name()),
                    true,
                    false
            ));
        } catch (Exception e) {
            System.err.println("Lỗi Signup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUser(){
        String sql = "select * from users";
        return executeFetch(sql, null, this::mapUser);
    }

    public boolean updateBalance(int id, double b){
        String sql = "UPDATE users SET BALANCE = ? WHERE id = ?";
        return executeUpdate(sql, List.of(b, id));
    }

    public boolean addBidderMetrics(int userId, double amount){
        String sql = "UPDATE users SET moneyspent = moneyspent  + ?, itemsbought = itemsbought + 1 WHERE id = ?";
        return executeUpdate(sql, List.of(amount, userId));
    }

    public boolean addSellerMetrics(int userId, double amount){
        String sql = "UPDATE users SET moneyreceived = moneyreceived + ?, itemssold = itemssold + 1 WHERE id = ?";
        return executeUpdate(sql, List.of(amount, userId));
    }

    public User getById(String id){
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> results = executeFetch(sql, List.of(id), this::mapUser);
        return results.isEmpty() ? null : results.get(0);
    }

    public User getByUsername(String username){
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> results = executeFetch(sql, List.of(username), this::mapUser);
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

    private String normalize(String value) {
        if (value == null) return "";
        return value.trim();
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