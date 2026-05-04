package com.auction.server.dao;

import com.auction.shared.ChatMessage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChatDao.class);

  private Connection getConn() {
    return DatabaseConnection.getInstance().getConnection();
  }

  public boolean insertMessage(ChatMessage msg) {
    String sql = "INSERT INTO chat_messages (sender_id, receiver_id, message_type, content) VALUES (?, ?, ?, ?)";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, msg.getSenderId());
      ps.setInt(2, msg.getReceiverId());
      ps.setString(3, msg.getMessageType());
      ps.setString(4, msg.getContent());
      int rows = ps.executeUpdate();
      if (rows > 0) {
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) msg.setId(keys.getInt(1));
      }
      return rows > 0;
    } catch (Exception e) {
      LOGGER.warn("insertMessage failed", e);
      return false;
    }
  }

  public List<ChatMessage> getGlobalHistory(int limit) {
    List<ChatMessage> messages = new ArrayList<>();
    String sql = "SELECT m.*, u.username AS sender_name, u.avatar_url AS sender_avatar "
        + "FROM chat_messages m LEFT JOIN users u ON m.sender_id = u.id "
        + "WHERE m.message_type = 'GLOBAL' ORDER BY m.created_at DESC LIMIT ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, limit);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) messages.add(0, mapRow(rs));
    } catch (Exception e) {
      LOGGER.warn("getGlobalHistory failed", e);
    }
    return messages;
  }

  public List<ChatMessage> getPrivateHistory(int userId1, int userId2, int limit) {
    List<ChatMessage> messages = new ArrayList<>();
    String sql = "SELECT m.*, u.username AS sender_name, u.avatar_url AS sender_avatar "
        + "FROM chat_messages m LEFT JOIN users u ON m.sender_id = u.id "
        + "WHERE m.message_type = 'PRIVATE' "
        + "AND ((m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?)) "
        + "ORDER BY m.created_at DESC LIMIT ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId1);
      ps.setInt(2, userId2);
      ps.setInt(3, userId2);
      ps.setInt(4, userId1);
      ps.setInt(5, limit);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) messages.add(0, mapRow(rs));
    } catch (Exception e) {
      LOGGER.warn("getPrivateHistory failed", e);
    }
    return messages;
  }

  /**
   * Returns distinct users that a given user has exchanged private messages with.
   */
  public List<Integer> getContactIds(int userId) {
    List<Integer> ids = new ArrayList<>();
    String sql = "SELECT DISTINCT CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END AS contact_id "
        + "FROM chat_messages WHERE message_type = 'PRIVATE' AND (sender_id = ? OR receiver_id = ?) "
        + "ORDER BY MAX(created_at) DESC";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ps.setInt(2, userId);
      ps.setInt(3, userId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) ids.add(rs.getInt("contact_id"));
    } catch (Exception e) {
      LOGGER.warn("getContactIds failed", e);
    }
    return ids;
  }

  private ChatMessage mapRow(ResultSet rs) throws SQLException {
    ChatMessage msg = new ChatMessage();
    msg.setId(rs.getInt("id"));
    msg.setSenderId(rs.getInt("sender_id"));
    msg.setReceiverId(rs.getInt("receiver_id"));
    msg.setMessageType(rs.getString("message_type"));
    msg.setContent(rs.getString("content"));
    Timestamp ts = rs.getTimestamp("created_at");
    if (ts != null) msg.setCreatedAt(ts.toLocalDateTime());
    try {
      msg.setSenderUsername(rs.getString("sender_name"));
      msg.setSenderAvatarUrl(rs.getString("sender_avatar"));
    } catch (SQLException e) { /* columns may not be present */ }
    return msg;
  }
}
