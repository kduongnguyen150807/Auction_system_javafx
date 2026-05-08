package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UserSchemaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserSchemaMigration.class);

  private UserSchemaMigration() {}

  static void applyStructure(Connection conn) {
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "fullname", "VARCHAR(255) NULL");
    MigrationSchemaSupport.executeIfColumnAdded(
        conn, "users", "fullname", "UPDATE users SET fullname = username WHERE fullname IS NULL OR TRIM(fullname) = ''");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "avgrating", "DOUBLE DEFAULT 0");
    MigrationSchemaSupport.addColumnIfMissing(conn, "users", "totalratings", "INT DEFAULT 0");

    MigrationSchemaSupport.createUniqueIndexIfMissing(conn, "users", "uk_users_username", "username");
    MigrationSchemaSupport.createUniqueIndexIfMissing(conn, "users", "uk_users_email", "email");
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
