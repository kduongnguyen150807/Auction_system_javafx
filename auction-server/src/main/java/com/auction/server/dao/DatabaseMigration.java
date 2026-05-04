package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseMigration {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigration.class);

  public static void runAll() {
    try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
      if (conn == null) {
        LOGGER.error("Cannot run migrations: no database connection");
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
      migratePasswordsToSha256(conn);
      LOGGER.info("Database migrations completed.");
    } catch (Exception e) {
      LOGGER.error("Migration failed", e);
    }
  }

  /**
   * One-time migration: converts any password that is NOT already a 64-character
   * lowercase hex string (SHA-256 output) into its SHA-256 hash using MySQL's
   * built-in {@code SHA2()} function.
   *
   * <p>MySQL's {@code SHA2(value, 256)} produces the same lowercase 64-char hex
   * digest as Java's {@code MessageDigest("SHA-256")}, so after this migration
   * the client-side {@link com.auction.shared.PasswordEncoder#hash} and the
   * stored value will always match.
   *
   * <p>The predicate {@code LENGTH(password) != 64} is a reliable sentinel
   * because: (a) plain-text passwords are almost never exactly 64 chars, and
   * (b) any previously migrated hash is always exactly 64 hex chars. The edge
   * case of a 64-char plain-text password is accepted as a known limitation of
   * this lightweight migration strategy.
   */
  private static void migratePasswordsToSha256(Connection conn) {
    String sql = "UPDATE users SET password = LOWER(SHA2(password, 256)) "
        + "WHERE LENGTH(password) <> 64 OR password REGEXP '[^0-9a-f]'";
    try (Statement st = conn.createStatement()) {
      int updated = st.executeUpdate(sql);
      if (updated > 0) {
        LOGGER.info("Password migration: hashed {} plain-text password(s) to SHA-256.", updated);
      } else {
        LOGGER.info("Password migration: all passwords already SHA-256 hashed, no changes needed.");
      }
    } catch (Exception e) {
      LOGGER.error("Password migration failed — users with plain-text passwords cannot log in", e);
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
      LOGGER.warn("Failed to ensure ratings table", e);
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
      LOGGER.warn("Failed to ensure transaction_logs table", e);
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
      LOGGER.warn("Failed to ensure chat_messages table", e);
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
      LOGGER.warn("Failed to ensure friendships table", e);
    }
  }

  private static void addColumnIfMissing(Connection conn, String table, String column, String definition) {
    try {
      if (!columnExists(conn, table, column)) {
        try (Statement st = conn.createStatement()) {
          st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
          LOGGER.info("Added column {}.{}", table, column);
        }
      }
    } catch (SQLException e) {
      LOGGER.warn("Failed to add column {}.{}", table, column, e);
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
      LOGGER.debug("Post-migration SQL failed", e);
    }
  }

  private static void createUniqueIndexIfMissing(Connection conn, String table, String indexName, String column) {
    try {
      if (!indexExists(conn, table, indexName)) {
        try (Statement st = conn.createStatement()) {
          st.execute("CREATE UNIQUE INDEX " + indexName + " ON " + table + "(" + column + ")");
          LOGGER.info("Created index {} on {}", indexName, table);
        }
      }
    } catch (SQLException e) {
      LOGGER.warn("Failed to create index {}", indexName, e);
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
