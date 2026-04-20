package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseMigration {
  private static final Logger LOGGER = Logger.getLogger(DatabaseMigration.class.getName());

  public static void runAll() {
    try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
      if (conn == null) {
        LOGGER.severe("Cannot run migrations: no database connection");
        return;
      }
      LOGGER.info("Running database migrations...");
      ensureUserColumns(conn);
      ensureUserIndexes(conn);
      ensureItemColumns(conn);
      ensureRatingsTable(conn);
      ensureTransactionLogTable(conn);
      ensureChatMessagesTable(conn);
      ensureFriendshipsTable(conn);
      LOGGER.info("Database migrations completed.");
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Migration failed", e);
    }
  }

  private static void ensureUserColumns(Connection conn) {
    addColumnIfMissing(conn, "users", "fullname", "VARCHAR(255) NULL");
    executeIfColumnAdded(conn, "users", "fullname",
        "UPDATE users SET fullname = username WHERE fullname IS NULL OR TRIM(fullname) = ''");
    addColumnIfMissing(conn, "users", "avgrating", "DOUBLE DEFAULT 0");
    addColumnIfMissing(conn, "users", "totalratings", "INT DEFAULT 0");
  }

  private static void ensureUserIndexes(Connection conn) {
    createUniqueIndexIfMissing(conn, "users", "uk_users_username", "username");
    createUniqueIndexIfMissing(conn, "users", "uk_users_email", "email");
  }

  private static void ensureItemColumns(Connection conn) {
    addColumnIfMissing(conn, "items", "image_url", "VARCHAR(2048) NULL");
    addColumnIfMissing(conn, "items", "maxprice", "DOUBLE NULL");
  }

  private static void ensureRatingsTable(Connection conn) {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
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
      LOGGER.log(Level.WARNING, "Failed to ensure ratings table", e);
    }
  }

  private static void ensureTransactionLogTable(Connection conn) {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
          "CREATE TABLE IF NOT EXISTS transaction_logs ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "userid INT NOT NULL, "
              + "type VARCHAR(50) NOT NULL, "
              + "amount DOUBLE NOT NULL, "
              + "itemid INT DEFAULT 0, "
              + "createdat DATETIME DEFAULT CURRENT_TIMESTAMP)");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to ensure transaction_logs table", e);
    }
  }

  private static void ensureChatMessagesTable(Connection conn) {
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
      LOGGER.log(Level.WARNING, "Failed to ensure chat_messages table", e);
    }
  }

  private static void ensureFriendshipsTable(Connection conn) {
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
      LOGGER.log(Level.WARNING, "Failed to ensure friendships table", e);
    }
  }

  private static void addColumnIfMissing(Connection conn, String table, String column, String definition) {
    try {
      if (!columnExists(conn, table, column)) {
        try (Statement st = conn.createStatement()) {
          st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
          LOGGER.info("Added column " + table + "." + column);
        }
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to add column " + table + "." + column, e);
    }
  }

  private static void executeIfColumnAdded(Connection conn, String table, String column, String sql) {
    try {
      if (columnExists(conn, table, column)) {
        try (Statement st = conn.createStatement()) {
          st.executeUpdate(sql);
        }
      }
    } catch (SQLException e) {
      LOGGER.log(Level.FINE, "Post-migration SQL failed", e);
    }
  }

  private static void createUniqueIndexIfMissing(Connection conn, String table, String indexName, String column) {
    try {
      if (!indexExists(conn, table, indexName)) {
        try (Statement st = conn.createStatement()) {
          st.execute("CREATE UNIQUE INDEX " + indexName + " ON " + table + "(" + column + ")");
          LOGGER.info("Created index " + indexName + " on " + table);
        }
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to create index " + indexName, e);
    }
  }

  private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
    String sql = "SELECT 1 FROM information_schema.columns "
        + "WHERE table_schema = database() AND table_name = ? AND column_name = ? LIMIT 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, table);
      ps.setString(2, column);
      try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }
  }

  private static boolean indexExists(Connection conn, String table, String indexName) throws SQLException {
    String sql = "SELECT 1 FROM information_schema.statistics "
        + "WHERE table_schema = database() AND table_name = ? AND index_name = ? LIMIT 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, table);
      ps.setString(2, indexName);
      try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }
  }
}
