package com.auction.server.dao.rating;

import com.auction.server.dao.platform.BaseDao;

import com.auction.shared.Rating;
import java.sql.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RatingDao extends BaseDao<Rating> implements RatingRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(RatingDao.class);

  @Override
  protected Rating mapRow(ResultSet rs) throws SQLException {
    Rating rating = new Rating();
    rating.setId(rs.getInt("id"));
    rating.setItemId(rs.getInt("itemid"));
    rating.setRaterUserId(rs.getInt("rateruserid"));
    rating.setRatedUserId(rs.getInt("rateduserid"));
    rating.setStars(rs.getInt("stars"));
    rating.setFeedback(rs.getString("feedback"));
    rating.setCreatedAt(rs.getTimestamp("createdat").toLocalDateTime());
    rating.setRaterUsername(rs.getString("rater_name"));
    return rating;
  }

  @Override
  public boolean insertRating(Rating rating) {
    return executeUpdate(
        "INSERT INTO ratings (itemid, rateruserid, rateduserid, stars, feedback) VALUES (?, ?, ?, ?, ?)",
        rating.getItemId(),
        rating.getRaterUserId(),
        rating.getRatedUserId(),
        rating.getStars(),
        rating.getFeedback());
  }

  @Override
  public boolean hasRated(int itemId, int userId) {
    String sql = "SELECT 1 FROM ratings WHERE itemid = ? AND rateruserid = ? LIMIT 1";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      ps.setInt(2, userId);
      ResultSet rs = ps.executeQuery();
      return rs.next();
    } catch (Exception e) {
      LOGGER.warn("hasRated check failed", e);
      return false;
    }
  }

  @Override
  public List<Rating> getByItemId(int itemId) {
    String sql = "SELECT r.*, u.username AS rater_name FROM ratings r "
        + "LEFT JOIN users u ON r.rateruserid = u.id WHERE r.itemid = ? ORDER BY r.createdat DESC";
    return queryList(sql, itemId);
  }

  @Override
  public void recalcUserRating(int userId) {
    try (Connection conn = getConn()) {
      String selectSql = "SELECT AVG(stars) AS avg_s, COUNT(*) AS cnt FROM ratings WHERE rateduserid = ?";
      try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
          double avgRating = rs.getDouble("avg_s");
          int totalRatings = rs.getInt("cnt");
          String updateSql = "UPDATE users SET avgrating = ?, totalratings = ? WHERE id = ?";
          try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setDouble(1, avgRating);
            updateStmt.setInt(2, totalRatings);
            updateStmt.setInt(3, userId);
            updateStmt.executeUpdate();
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("recalcUserRating failed", e);
    }
  }

}
