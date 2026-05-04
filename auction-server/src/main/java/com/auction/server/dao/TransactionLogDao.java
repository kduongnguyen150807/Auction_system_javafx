package com.auction.server.dao;

import com.auction.shared.TransactionLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionLogDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogDao.class);

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
      LOGGER.warn("insertLog failed", e);
      return false;
    }
  }

  /** Caller-managed connection variant for inclusion in a wider transaction. */
  public boolean insertLogTx(
      int userId, String type, double amount, int itemId, Connection conn) throws SQLException {
    String sql = "INSERT INTO transaction_logs (userid, type, amount, itemid) VALUES (?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ps.setString(2, type);
      ps.setDouble(3, amount);
      ps.setInt(4, itemId);
      return ps.executeUpdate() > 0;
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
      LOGGER.warn("getByUserId failed", e);
    }
    return logs;
  }
}
