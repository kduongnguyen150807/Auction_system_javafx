package com.auction.server.dao.wallet;

import com.auction.server.dao.platform.BaseDao;
import com.auction.shared.TransactionLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class TransactionLogDao extends BaseDao<TransactionLog> {

  public static final String TYPE_DEPOSIT = "DEPOSIT";
  public static final String TYPE_PAYMENT = "PAYMENT";
  public static final String TYPE_RECEIVE = "RECEIVE";
  public static final String TYPE_REFUND = "REFUND";
  public static final String TYPE_BID_HOLD = "BID_HOLD";

  private static final String INSERT_TRANSACTION_LOG_SQL =
          "INSERT INTO transaction_logs (userid, type, amount, itemid) VALUES (?, ?, ?, ?)";

  private static final String SELECT_TRANSACTION_LOGS_BY_USER_SQL =
          "SELECT * FROM transaction_logs WHERE userid = ? ORDER BY createdat DESC";

  @Override
  protected TransactionLog mapRow(ResultSet rs) throws SQLException {
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

  public boolean insertLog(int userId, String type, double amount, int itemId) {
    if (!isValidLogInput(type, amount)) {
      return false;
    }

    return executeUpdate(
            INSERT_TRANSACTION_LOG_SQL,
            userId,
            normalizeType(type),
            amount,
            itemId);
  }

  /**
   * Caller-managed connection variant for inclusion in a wider transaction.
   */
  public boolean insertLogTx(
          int userId,
          String type,
          double amount,
          int itemId,
          Connection conn)
          throws SQLException {

    if (!isValidLogInput(type, amount)) {
      return false;
    }

    try (PreparedStatement ps = conn.prepareStatement(INSERT_TRANSACTION_LOG_SQL)) {
      ps.setInt(1, userId);
      ps.setString(2, normalizeType(type));
      ps.setDouble(3, amount);
      ps.setInt(4, itemId);

      return ps.executeUpdate() > 0;
    }
  }

  public List<TransactionLog> getByUserId(int userId) {
    return queryList(SELECT_TRANSACTION_LOGS_BY_USER_SQL, userId);
  }

  private boolean isValidLogInput(String type, double amount) {
    return isValidTransactionType(type) && amount > 0;
  }

  private boolean isValidTransactionType(String type) {
    String normalizedType = normalizeType(type);

    return TYPE_DEPOSIT.equals(normalizedType)
            || TYPE_PAYMENT.equals(normalizedType)
            || TYPE_RECEIVE.equals(normalizedType)
            || TYPE_REFUND.equals(normalizedType)
            || TYPE_BID_HOLD.equals(normalizedType);
  }

  private String normalizeType(String type) {
    if (type == null) {
      return "";
    }

    return type.trim().toUpperCase();
  }
}