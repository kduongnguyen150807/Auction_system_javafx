package com.auction.server.service;

import com.auction.server.dao.TransactionLogDao;
import com.auction.shared.TransactionLog;
import java.util.List;

public class TransactionService {
    private final TransactionLogDao logDao;

    public TransactionService() {
        this(new TransactionLogDao());
    }

    public TransactionService(TransactionLogDao logDao) {
        this.logDao = logDao;
    }

    public List<TransactionLog> getTransactions(int userId) {
        return logDao.getByUserId(userId);
    }
}