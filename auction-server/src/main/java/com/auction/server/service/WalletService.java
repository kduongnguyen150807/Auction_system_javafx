package com.auction.server.service;

import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.User;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WalletService {
    private static final Logger LOGGER = Logger.getLogger(WalletService.class.getName());

    private final UserDao userDao;
    private final TransactionLogDao logDao;

    public WalletService() {
        this(new UserDao(), new TransactionLogDao());
    }

    public WalletService(UserDao userDao, TransactionLogDao logDao) {
        this.userDao = userDao;
        this.logDao = logDao;
    }

    public User deposit(int userId, double amount) {
        if (amount <= 0) {
            return null;
        }

        User user = userDao.getById(String.valueOf(userId));
        if (user == null) {
            return null;
        }

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            try {
                double newBalance = user.getBalance() + amount;

                boolean balanceUpdated = userDao.updateBalance(connection, userId, newBalance);
                boolean logInserted = logDao.insertLog(connection, userId, "DEPOSIT", amount, 0);

                if (!balanceUpdated || !logInserted) {
                    connection.rollback();
                    return null;
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to deposit money", e);
            return null;
        }

        return userDao.getById(String.valueOf(userId));
    }
}