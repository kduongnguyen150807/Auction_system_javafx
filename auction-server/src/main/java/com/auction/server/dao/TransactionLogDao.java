package com.auction.server.dao;

import com.auction.server.Service.SQLService;
import com.auction.shared.TransactionLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionLogDao {
    private static TransactionLogDao instance;
    private Connection conn;

    private TransactionLogDao() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public static TransactionLogDao getInstance() {
        if (instance == null) {
            instance = new TransactionLogDao();
        }
        return instance;
    }

    public boolean insertLog(int u, String t, double a, int i) {
        String sql = "INSERT INTO transaction_logs (userid, type, amount, itemid) VALUES (?, ?, ?, ?)";
        return SQLService.Update(sql, List.of(i,t,a,i), this.conn);
    }

    public List<TransactionLog> getByUserId(int userId) {
        String sql = "SELECT * FROM transaction_logs WHERE userid = ? ORDER BY createdat DESC";
        return SQLService.Fetch(sql, List.of(userId), this.conn, this::mapResultSet);
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