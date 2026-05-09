package com.auction.server.dao.chat;

import com.auction.server.dao.platform.BaseDao;

import com.auction.shared.Friendship;
import java.sql.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendDao extends BaseDao<Friendship> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FriendDao.class);

  @Override
  protected Friendship mapRow(ResultSet rs) throws SQLException {
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

  public boolean sendRequest(int requesterId, int addresseeId) {
    return executeUpdate(
        "INSERT IGNORE INTO friendships (requester_id, addressee_id, status) VALUES (?, ?, 'PENDING')",
        requesterId,
        addresseeId);
  }

  public boolean acceptRequest(int requesterId, int addresseeId) {
    return executeUpdate(
        "UPDATE friendships SET status = 'ACCEPTED' WHERE requester_id = ? AND addressee_id = ? AND status = 'PENDING'",
        requesterId,
        addresseeId);
  }

  public boolean declineRequest(int requesterId, int addresseeId) {
    return executeUpdate(
        "DELETE FROM friendships WHERE requester_id = ? AND addressee_id = ? AND status = 'PENDING'",
        requesterId,
        addresseeId);
  }

  public boolean removeFriend(int userId1, int userId2) {
    return executeUpdate(
        "DELETE FROM friendships WHERE "
            + "((requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)) "
            + "AND status = 'ACCEPTED'",
        userId1,
        userId2,
        userId2,
        userId1);
  }

  public List<Friendship> getFriends(int userId) {
    String sql = "SELECT f.*, "
        + "u1.username AS req_name, u1.avatar_url AS req_avatar, "
        + "u2.username AS addr_name, u2.avatar_url AS addr_avatar "
        + "FROM friendships f "
        + "LEFT JOIN users u1 ON f.requester_id = u1.id "
        + "LEFT JOIN users u2 ON f.addressee_id = u2.id "
        + "WHERE (f.requester_id = ? OR f.addressee_id = ?) AND f.status = 'ACCEPTED' "
        + "ORDER BY f.created_at DESC";
    return queryList(sql, userId, userId);
  }

  public List<Friendship> getPendingRequests(int addresseeId) {
    String sql = "SELECT f.*, "
        + "u1.username AS req_name, u1.avatar_url AS req_avatar, "
        + "u2.username AS addr_name, u2.avatar_url AS addr_avatar "
        + "FROM friendships f "
        + "LEFT JOIN users u1 ON f.requester_id = u1.id "
        + "LEFT JOIN users u2 ON f.addressee_id = u2.id "
        + "WHERE f.addressee_id = ? AND f.status = 'PENDING' "
        + "ORDER BY f.created_at DESC";
    return queryList(sql, addresseeId);
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
      LOGGER.warn("getRelationship failed", e);
    }
    return null;
  }
}
