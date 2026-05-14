package com.auction.server.repository;

import com.auction.server.dao.UserDao;
import com.auction.shared.user.User;

import java.sql.Connection;

public class UserRepository {

  private final UserDao userDao;

  public UserRepository(UserDao userDao) {
    this.userDao = userDao;
  }

  public User login(String username, String password, Connection connection) {
    return userDao.login(username, password, connection);
  }

  public boolean register(
    String username, String password, String email, int age,
    Connection connection) {
    return userDao.register(username, password, email, age, connection);
  }

  public User findById(int id, Connection connection) {
    return userDao.findById(id, connection);
  }

  public User findForUpdate(int userId, Connection connection) {
    return userDao.findForUpdate(userId, connection);
  }

  public boolean updateBalance(int userId, double amount,  Connection connection) {
    return userDao.updateBalance(userId, amount, connection);
  }
}
