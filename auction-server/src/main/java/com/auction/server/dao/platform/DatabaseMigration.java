package com.auction.server.dao.platform;

import java.sql.Connection;
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
      UserSchemaMigration.applyStructure(conn);
      ItemSchemaMigration.apply(conn);
      RatingsSchemaMigration.apply(conn);
      TransactionLogSchemaMigration.apply(conn);
      ChatSchemaMigration.apply(conn);
      FriendshipSchemaMigration.apply(conn);
      UserSchemaMigration.migratePasswordsToSha256(conn);
      LOGGER.info("Database migrations completed.");
    } catch (Exception e) {
      LOGGER.error("Migration failed", e);
    }
  }
}
