package com.auction.server.dao;

import com.auction.shared.Rating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RatingDao implements RatingRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(RatingDao.class);

  private Connection getConn() {
    return DatabaseConnection.getInstance().getConnection();
  }

  @Override
  public boolean insertRating(Rating rating) {
    String sql = "INSERT INTO ratings (itemid, rateruserid, rateduserid, stars, feedback) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, rating.getItemId());
      ps.setInt(2, rating.getRaterUserId());
      ps.setInt(3, rating.getRatedUserId());
      ps.setInt(4, rating.getStars());
      ps.setString(5, rating.getFeedback());
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.warn("insertRating failed", e);
      return false;
    }
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
    List<Rating> ratings = new ArrayList<>();
    String sql = "SELECT r.*, u.username AS rater_name FROM ratings r "
        + "LEFT JOIN users u ON r.rateruserid = u.id WHERE r.itemid = ? ORDER BY r.createdat DESC";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) ratings.add(mapResultSetToRating(rs));
    } catch (Exception e) {
      LOGGER.warn("getByItemId failed", e);
    }
    return ratings;
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

  private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
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
}
