package com.auction.server.dao.platform;

import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseMigration {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigration.class);

  private DatabaseMigration() {}

  public static void runAll() {
    try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
      LOGGER.info("Running database migrations...");

      UserSchemaMigration.applyStructure(conn);
      ItemSchemaMigration.apply(conn);
      BidTransactionsSchemaMigration.apply(conn);
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