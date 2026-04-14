package com.auction.server.dao;

import com.auction.server.Factory.UserFactory;
import com.auction.shared.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    private static UserDao instance;
    private final Connection connection;

    private UserDao() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public User login(String username, String password) {
        User authenticatedUser = null;
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND isactive = true";

        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    authenticatedUser = mapUser(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn đăng nhập: " + e.getMessage());
        }
        return authenticatedUser;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
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

        return user;
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