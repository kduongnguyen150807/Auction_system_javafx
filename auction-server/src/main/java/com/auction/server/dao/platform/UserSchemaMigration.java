package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UserSchemaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserSchemaMigration.class);

  private UserSchemaMigration() {}

  static void applyStructure(Connection conn) {
    ensureUsersTable(conn);
    alignUsersColumnNames(conn);
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "fullname", "VARCHAR(255) NULL");
    MigrationSchemaSupport.executeIfColumnAdded(
        conn, "users", "fullname", "UPDATE users SET fullname = username WHERE fullname IS NULL OR TRIM(fullname) = ''");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "phone_number", "VARCHAR(64) NULL");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "is_active", "TINYINT(1) NOT NULL DEFAULT 1");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "is_locked", "TINYINT(1) NOT NULL DEFAULT 0");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "money_spent", "DOUBLE NOT NULL DEFAULT 0");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "items_bought", "INT NOT NULL DEFAULT 0");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "money_received", "DOUBLE NOT NULL DEFAULT 0");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "items_sold", "INT NOT NULL DEFAULT 0");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "avg_rating", "DOUBLE NOT NULL DEFAULT 0");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "total_ratings", "INT NOT NULL DEFAULT 0");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "last_login_at", "DATETIME NULL");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "session_token", "VARCHAR(64) NULL");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "vip_until", "DATETIME NULL");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "last_daily_spin_at", "DATETIME NULL");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "paid_spin_credits", "INT NOT NULL DEFAULT 0");

    MigrationSchemaSupport.createUniqueIndexIfMissing(conn, "users", "uk_users_username", "username");
    MigrationSchemaSupport.createUniqueIndexIfMissing(conn, "users", "uk_users_email", "email");
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
              "CREATE TABLE IF NOT EXISTS watchlists ("
                      + "user_id INT NOT NULL, "
                      + "item_id INT NOT NULL, "
                      + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                      + "PRIMARY KEY (user_id, item_id), "
                      + "INDEX idx_watchlist_item (item_id))"); // Index để sau này truy vấn "Ai đang theo dõi item này" cực nhanh
    } catch (Exception e) {
      LOGGER.warn("Failed to ensure watchlists table", e);
    }
  }

  private static void ensureUsersTable(Connection conn) {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
          "CREATE TABLE IF NOT EXISTS users ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "version INT NOT NULL DEFAULT 0, "
              + "username VARCHAR(100) NOT NULL, "
              + "fullname VARCHAR(255) NULL, "
              + "password VARCHAR(255) NOT NULL, "
              + "email VARCHAR(255) NOT NULL, "
              + "age VARCHAR(32) NULL, "
              + "phone_number VARCHAR(64) NULL, "
              + "role VARCHAR(20) NOT NULL, "
              + "balance DOUBLE NOT NULL DEFAULT 0, "
              + "money_spent DOUBLE NOT NULL DEFAULT 0, "
              + "items_bought INT NOT NULL DEFAULT 0, "
              + "money_received DOUBLE NOT NULL DEFAULT 0, "
              + "items_sold INT NOT NULL DEFAULT 0, "
              + "is_active TINYINT(1) NOT NULL DEFAULT 1, "
              + "is_locked TINYINT(1) NOT NULL DEFAULT 0, "
              + "avatar_url VARCHAR(2048) NULL, "
              + "avg_rating DOUBLE NOT NULL DEFAULT 0, "
              + "total_ratings INT NOT NULL DEFAULT 0, "
              + "last_login_at DATETIME NULL, "
              + "session_token VARCHAR(64) NULL, "
              + "UNIQUE KEY uk_users_username (username), "
              + "UNIQUE KEY uk_users_email (email)"
              + ")");
    } catch (Exception e) {
      LOGGER.warn("Failed to ensure users table", e);
    }
  }

  /** Renames legacy users columns to names expected by {@link com.auction.server.dao.user.UserDao}. */
  private static void alignUsersColumnNames(Connection conn) {
    MigrationSchemaSupport.renameColumnIfExists(
        conn, "users", "phonenumber", "phone_number", "VARCHAR(64) NULL");
    MigrationSchemaSupport.renameColumnIfExists(
        conn, "users", "moneyspent", "money_spent", "DOUBLE NOT NULL DEFAULT 0");
    MigrationSchemaSupport.renameColumnIfExists(
        conn, "users", "itemsbought", "items_bought", "INT NOT NULL DEFAULT 0");
    MigrationSchemaSupport.renameColumnIfExists(
        conn, "users", "moneyreceived", "money_received", "DOUBLE NOT NULL DEFAULT 0");
    MigrationSchemaSupport.renameColumnIfExists(
        conn, "users", "itemssold", "items_sold", "INT NOT NULL DEFAULT 0");
    MigrationSchemaSupport.renameColumnIfExists(
        conn, "users", "isactive", "is_active", "TINYINT(1) NOT NULL DEFAULT 1");
    MigrationSchemaSupport.renameColumnIfExists(
        conn, "users", "islocked", "is_locked", "TINYINT(1) NOT NULL DEFAULT 0");
    MigrationSchemaSupport.renameColumnIfExists(
        conn, "users", "avgrating", "avg_rating", "DOUBLE NOT NULL DEFAULT 0");
    MigrationSchemaSupport.renameColumnIfExists(
        conn, "users", "totalratings", "total_ratings", "INT NOT NULL DEFAULT 0");
  }

  /**
   * One-time migration: converts any password that is NOT already a 64-character lowercase hex string
   * (SHA-256 output) into its SHA-256 hash using MySQL's {@code SHA2()} function.
   */
  static void migratePasswordsToSha256(Connection conn) {
    String sql =
        "UPDATE users SET password = LOWER(SHA2(password, 256)) "
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
}
