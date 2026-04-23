package com.auction.server.service;

import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.User;

public class WalletService {
    private final UserDao userDao;
    private final TransactionLogDao logDao;

    public WalletService() {
        this.userDao = new UserDao();
        this.logDao = new TransactionLogDao();
    }

    public User deposit(int userId, double amount) {
        if (amount <= 0) return null;

        User u = userDao.getById(String.valueOf(userId));
        if (u == null) return null;

        double newBalance = u.getBalance() + amount;
        boolean ok = userDao.updateBalance(userId, newBalance);
        if (!ok) return null;

        logDao.insertLog(userId, "DEPOSIT", amount, 0);
        return userDao.getById(String.valueOf(userId));
    }
}