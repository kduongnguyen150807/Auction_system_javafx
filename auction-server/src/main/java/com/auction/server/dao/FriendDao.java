package com.auction.server.dao;

import com.auction.shared.Friendship;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FriendDao {
  private static final Logger LOGGER = Logger.getLogger(FriendDao.class.getName());

  private Connection getConn() {
    return DatabaseConnection.getInstance().getConnection();
  }

  public boolean sendRequest(int requesterId, int addresseeId) {
    String sql = "INSERT IGNORE INTO friendships (requester_id, addressee_id, status) VALUES (?, ?, 'PENDING')";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, requesterId);
      ps.setInt(2, addresseeId);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "sendRequest failed", e);
      return false;
    }
  }

  public boolean acceptRequest(int requesterId, int addresseeId) {
    String sql = "UPDATE friendships SET status = 'ACCEPTED' WHERE requester_id = ? AND addressee_id = ? AND status = 'PENDING'";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, requesterId);
      ps.setInt(2, addresseeId);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "acceptRequest failed", e);
      return false;
    }
  }

  public boolean declineRequest(int requesterId, int addresseeId) {
    String sql = "DELETE FROM friendships WHERE requester_id = ? AND addressee_id = ? AND status = 'PENDING'";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, requesterId);
      ps.setInt(2, addresseeId);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "declineRequest failed", e);
      return false;
    }
  }

  public boolean removeFriend(int userId1, int userId2) {
    String sql = "DELETE FROM friendships WHERE "
        + "((requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)) "
        + "AND status = 'ACCEPTED'";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId1);
      ps.setInt(2, userId2);
      ps.setInt(3, userId2);
      ps.setInt(4, userId1);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "removeFriend failed", e);
      return false;
    }
  }

  public List<Friendship> getFriends(int userId) {
    List<Friendship> list = new ArrayList<>();
    String sql = "SELECT f.*, "
        + "u1.username AS req_name, u1.avatar_url AS req_avatar, "
        + "u2.username AS addr_name, u2.avatar_url AS addr_avatar "
        + "FROM friendships f "
        + "LEFT JOIN users u1 ON f.requester_id = u1.id "
        + "LEFT JOIN users u2 ON f.addressee_id = u2.id "
        + "WHERE (f.requester_id = ? OR f.addressee_id = ?) AND f.status = 'ACCEPTED' "
        + "ORDER BY f.created_at DESC";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ps.setInt(2, userId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) list.add(mapRow(rs));
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "getFriends failed", e);
    }
    return list;
  }

  public List<Friendship> getPendingRequests(int addresseeId) {
    List<Friendship> list = new ArrayList<>();
    String sql = "SELECT f.*, "
        + "u1.username AS req_name, u1.avatar_url AS req_avatar, "
        + "u2.username AS addr_name, u2.avatar_url AS addr_avatar "
        + "FROM friendships f "
        + "LEFT JOIN users u1 ON f.requester_id = u1.id "
        + "LEFT JOIN users u2 ON f.addressee_id = u2.id "
        + "WHERE f.addressee_id = ? AND f.status = 'PENDING' "
        + "ORDER BY f.created_at DESC";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, addresseeId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) list.add(mapRow(rs));
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "getPendingRequests failed", e);
    }
    return list;
  }

  public String getRelationship(int userId1, int userId2) {
    String sql = "SELECT status, requester_id FROM friendships WHERE "
        + "((requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)) LIMIT 1";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId1);
      ps.setInt(2, userId2);
      ps.setInt(3, userId2);
      ps.setInt(4, userId1);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return rs.getString("status");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "getRelationship failed", e);
    }
    return null;
  }

  private Friendship mapRow(ResultSet rs) throws SQLException {
    Friendship f = new Friendship();
    f.setId(rs.getInt("id"));
    f.setRequesterId(rs.getInt("requester_id"));
    f.setAddresseeId(rs.getInt("addressee_id"));
    f.setStatus(rs.getString("status"));
    Timestamp ts = rs.getTimestamp("created_at");
    if (ts != null) f.setCreatedAt(ts.toLocalDateTime());
    try {
      f.setRequesterUsername(rs.getString("req_name"));
      f.setRequesterAvatarUrl(rs.getString("req_avatar"));
      f.setAddresseeUsername(rs.getString("addr_name"));
      f.setAddresseeAvatarUrl(rs.getString("addr_avatar"));
    } catch (SQLException e) { /* join columns may be absent */ }
    return f;
  }
}
