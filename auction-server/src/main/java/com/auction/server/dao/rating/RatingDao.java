package com.auction.server.dao.rating;

import com.auction.server.dao.platform.BaseDao;
import com.auction.shared.Rating;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
//Chống sập do bị tràn cursor DB
public class RatingDao extends BaseDao<Rating> implements RatingRepository {

  @Override
  protected Rating mapRow(ResultSet rs) throws SQLException {
    Rating ans = new Rating();
    ans.setId(rs.getInt("id"));
    ans.setItemId(rs.getInt("itemid"));
    ans.setRaterUserId(rs.getInt("rateruserid"));
    ans.setRatedUserId(rs.getInt("rateduserid"));
    ans.setStars(rs.getInt("stars"));
    ans.setFeedback(rs.getString("feedback"));
    ans.setCreatedAt(rs.getTimestamp("createdat").toLocalDateTime());
    ans.setRaterUsername(rs.getString("rater_name"));
    return ans;
  }
  @Override
  public boolean insertRating(Rating rating) {
    boolean ans = executeUpdate(
            "INSERT INTO ratings (itemid, rateruserid, rateduserid, stars, feedback) VALUES (?, ?, ?, ?, ?)",
            rating.getItemId(),
            rating.getRaterUserId(),
            rating.getRatedUserId(),
            rating.getStars(),
            rating.getFeedback());
    return ans;
  }

  @Override
  public boolean hasRated(int itemid, int userid) {
    String sql = "SELECT 1 FROM ratings WHERE itemid = ? AND rateruserid = ? LIMIT 1";
    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemid);
      ps.setInt(2, userid);
      try (ResultSet ans = ps.executeQuery()) {
        return ans.next();
      }
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public List<Rating> getByItemId(int itemid) {
    String sql = "SELECT r.*, u.username AS rater_name FROM ratings r LEFT JOIN users u ON r.rateruserid = u.id WHERE r.itemid = ? ORDER BY r.createdat DESC";
    List<Rating> ans = queryList(sql, itemid);
    return ans;
  }

  @Override
  public void recalcUserRating(int userid) {
    try (Connection conn = getConn()) {
      String selectsql = "SELECT AVG(stars) AS avg_s, COUNT(*) AS cnt FROM ratings WHERE rateduserid = ?";
      try (PreparedStatement ps = conn.prepareStatement(selectsql)) {
        ps.setInt(1, userid);
        try (ResultSet ans = ps.executeQuery()) {
          if (ans.next()) {
            double avgrating = ans.getDouble("avg_s");
            int totalratings = ans.getInt("cnt");
            String updatesql = "UPDATE users SET avgrating = ?, totalratings = ? WHERE id = ?";
            try (PreparedStatement updatestmt = conn.prepareStatement(updatesql)) {
              updatestmt.setDouble(1, avgrating);
              updatestmt.setInt(2, totalratings);
              updatestmt.setInt(3, userid);
              updatestmt.executeUpdate();
            }
          }
        }
      }
    } catch (Exception e) {
    }
  }
}