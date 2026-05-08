package com.auction.server.dao.wallet;

import com.auction.server.dao.platform.BaseDao;

import com.auction.shared.TransactionLog;
import java.sql.*;
import java.util.List;

public class TransactionLogDao extends BaseDao<TransactionLog> {

  @Override
  protected TransactionLog mapRow(ResultSet rs) throws SQLException {
    TransactionLog log = new TransactionLog();
    log.setId(rs.getInt("id"));
    log.setUserId(rs.getInt("userid"));
    log.setType(rs.getString("type"));
    log.setAmount(rs.getDouble("amount"));
    log.setItemId(rs.getInt("itemid"));
    log.setCreatedAt(rs.getTimestamp("createdat").toLocalDateTime());
    return log;
  }

  public boolean insertLog(int userId, String type, double amount, int itemId) {
    return executeUpdate(
        "INSERT INTO transaction_logs (userid, type, amount, itemid) VALUES (?, ?, ?, ?)",
        userId,
        type,
        amount,
        itemId);
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
    return queryList("SELECT * FROM transaction_logs WHERE userid = ? ORDER BY createdat DESC", userId);
  }
}
