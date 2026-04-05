package com.auction.server.dao;

import com.auction.shared.Rating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingDao {
  private Connection conn;

  public RatingDao() {
    this.conn = DatabaseConnection.getInstance().getConnection();
    ensureTable();
  }

  private void ensureTable() {
    try {
      Statement res = this.conn.createStatement();
      res.executeUpdate(
          "CREATE TABLE IF NOT EXISTS ratings ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "itemid INT NOT NULL, "
              + "rateruserid INT NOT NULL, "
              + "rateduserid INT NOT NULL, "
              + "stars INT NOT NULL, "
              + "feedback TEXT, "
              + "createdat DATETIME DEFAULT CURRENT_TIMESTAMP, "
              + "UNIQUE KEY uq_rating (itemid, rateruserid))");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean insertRating(Rating r) {
    boolean ans = false;
    try {
      String res =
          "INSERT INTO ratings (itemid, rateruserid, rateduserid, stars, feedback) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement res1 = this.conn.prepareStatement(res);
      res1.setInt(1, r.getItemId());
      res1.setInt(2, r.getRaterUserId());
      res1.setInt(3, r.getRatedUserId());
      res1.setInt(4, r.getStars());
      res1.setString(5, r.getFeedback());
      int res2 = res1.executeUpdate();
      ans = res2 > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean hasRated(int itemId, int userId) {
    boolean ans = false;
    try {
      String res = "SELECT 1 FROM ratings WHERE itemid = ? AND rateruserid = ? LIMIT 1";
      PreparedStatement res1 = this.conn.prepareStatement(res);
      res1.setInt(1, itemId);
      res1.setInt(2, userId);
      ResultSet res2 = res1.executeQuery();
      ans = res2.next();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public List<Rating> getByItemId(int itemId) {
    List<Rating> ans = new ArrayList<>();
    try {
      String res =
          "SELECT r.*, u.username AS rater_name FROM ratings r LEFT JOIN users u ON r.rateruserid = u.id WHERE r.itemid = ? ORDER BY r.createdat DESC";
      PreparedStatement res1 = this.conn.prepareStatement(res);
      res1.setInt(1, itemId);
      ResultSet res2 = res1.executeQuery();
      while (res2.next()) {
        Rating res3 = new Rating();
        res3.setId(res2.getInt("id"));
        res3.setItemId(res2.getInt("itemid"));
        res3.setRaterUserId(res2.getInt("rateruserid"));
        res3.setRatedUserId(res2.getInt("rateduserid"));
        res3.setStars(res2.getInt("stars"));
        res3.setFeedback(res2.getString("feedback"));
        res3.setCreatedAt(res2.getTimestamp("createdat").toLocalDateTime());
        res3.setRaterUsername(res2.getString("rater_name"));
        ans.add(res3);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public void recalcUserRating(int userId) {
    try {
      String res = "SELECT AVG(stars) AS avg_s, COUNT(*) AS cnt FROM ratings WHERE rateduserid = ?";
      PreparedStatement res1 = this.conn.prepareStatement(res);
      res1.setInt(1, userId);
      ResultSet res2 = res1.executeQuery();
      if (res2.next()) {
        double ans = res2.getDouble("avg_s");
        int ans1 = res2.getInt("cnt");
        String res3 = "UPDATE users SET avgrating = ?, totalratings = ? WHERE id = ?";
        PreparedStatement res4 = this.conn.prepareStatement(res3);
        res4.setDouble(1, ans);
        res4.setInt(2, ans1);
        res4.setInt(3, userId);
        res4.executeUpdate();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
