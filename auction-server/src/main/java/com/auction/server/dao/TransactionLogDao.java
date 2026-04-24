package com.auction.server.dao;

import com.auction.shared.TransactionLog;
import java.sql.*;
import java.util.List;

public class TransactionLogDao extends BaseDao{
    private static TransactionLogDao instance;

    private TransactionLogDao() {}

    public static TransactionLogDao getInstance() {
        if (instance == null) {
            instance = new TransactionLogDao();
        }
        return instance;
    }

    public boolean insertLog(int u, String t, double a, int i) {
        String sql = "INSERT INTO transaction_logs (userid, type, amount, itemid) VALUES (?, ?, ?, ?)";
        return executeUpdate(sql, List.of(u,t,a,i));
    }

    public List<TransactionLog> getByUserId(int userId) {
        String sql = "SELECT * FROM transaction_logs WHERE userid = ? ORDER BY createdat DESC";
        return executeFetch(sql, List.of(userId), this::mapResultSet);
    }

    private TransactionLog mapResultSet(ResultSet rs) throws SQLException {
        TransactionLog log = new TransactionLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("userid"));
        log.setType(rs.getString("type"));
        log.setAmount(rs.getDouble("amount"));
        log.setItemId(rs.getInt("itemid"));

        Timestamp createdAt = rs.getTimestamp("createdat");
        if (createdAt != null) {
            log.setCreatedAt(createdAt.toLocalDateTime());
        }
        return log;
    }
}