package com.auction.server.dao;

import com.auction.shared.TransactionLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionLogDao {
  private static final Logger LOGGER = Logger.getLogger(TransactionLogDao.class.getName());

  private Connection getConn() {
    return DatabaseConnection.getInstance().getConnection();
  }

  public boolean insertLog(int userId, String type, double amount, int itemId) {
    String sql = "INSERT INTO transaction_logs (userid, type, amount, itemid) VALUES (?, ?, ?, ?)";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ps.setString(2, type);
      ps.setDouble(3, amount);
      ps.setInt(4, itemId);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "insertLog failed", e);
      return false;
    }
  }

  public List<TransactionLog> getByUserId(int userId) {
    List<TransactionLog> logs = new ArrayList<>();
    String sql = "SELECT * FROM transaction_logs WHERE userid = ? ORDER BY createdat DESC";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        TransactionLog log = new TransactionLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("userid"));
        log.setType(rs.getString("type"));
        log.setAmount(rs.getDouble("amount"));
        log.setItemId(rs.getInt("itemid"));
        log.setCreatedAt(rs.getTimestamp("createdat").toLocalDateTime());
        logs.add(log);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "getByUserId failed", e);
    }
    return logs;
  }
}
