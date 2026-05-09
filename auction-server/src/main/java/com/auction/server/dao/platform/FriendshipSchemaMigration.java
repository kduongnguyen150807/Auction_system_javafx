package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class FriendshipSchemaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(FriendshipSchemaMigration.class);

  private FriendshipSchemaMigration() {}

  static void apply(Connection conn) {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
          "CREATE TABLE IF NOT EXISTS friendships ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "requester_id INT NOT NULL, "
              + "addressee_id INT NOT NULL, "
              + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', "
              + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
              + "UNIQUE KEY uq_friendship (requester_id, addressee_id), "
              + "INDEX idx_addressee (addressee_id, status))");
    } catch (Exception e) {
      LOGGER.warn("Failed to ensure friendships table", e);
    }
  }
}
