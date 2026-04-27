package com.auction.server.dao;

import com.auction.shared.TransactionLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionLogDao {

  public TransactionLogDao() {

  }

  public boolean insertLog(int u, String t, double a, int i) {
    boolean ans = false;
    String sql =
            "INSERT INTO transaction_logs (userid, type, amount, itemid) VALUES (?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, u);
      ps.setString(2, t);
      ps.setDouble(3, a);
      ps.setInt(4, i);
      int res2 = ps.executeUpdate();
      ans = res2 > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }
  public boolean insertLog(Connection conn, int userId, String type, double amount, int itemId) {
    String sql = "insert into transaction_logs(userid, type, amount, itemid, timestamp) values(?,?,?,?,NOW())";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ps.setString(2, type);
      ps.setDouble(3, amount);
      ps.setInt(4, itemId);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public List<TransactionLog> getByUserId(int u) {
    List<TransactionLog> ans = new ArrayList<>();
    String sql = "SELECT * FROM transaction_logs WHERE userid = ? ORDER BY createdat DESC";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, u);
      ResultSet res2 = ps.executeQuery();
      while (res2.next()) {
        TransactionLog res3 = new TransactionLog();
        res3.setId(res2.getInt("id"));
        res3.setUserId(res2.getInt("userid"));
        res3.setType(res2.getString("type"));
        res3.setAmount(res2.getDouble("amount"));
        res3.setItemId(res2.getInt("itemid"));
        res3.setCreatedAt(res2.getTimestamp("createdat").toLocalDateTime());
        ans.add(res3);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }
}
