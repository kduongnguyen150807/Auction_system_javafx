package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ChatSchemaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChatSchemaMigration.class);

  private ChatSchemaMigration() {}

  static void apply(Connection conn) {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
          "CREATE TABLE IF NOT EXISTS chat_messages ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "sender_id INT NOT NULL, "
              + "receiver_id INT DEFAULT 0, "
              + "message_type VARCHAR(20) NOT NULL DEFAULT 'GLOBAL', "
              + "content TEXT NOT NULL, "
              + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
              + "INDEX idx_chat_global (message_type, created_at), "
              + "INDEX idx_chat_private (sender_id, receiver_id, created_at))");
    } catch (Exception e) {
      LOGGER.warn("Failed to ensure chat_messages table", e);
    }
  }
}
