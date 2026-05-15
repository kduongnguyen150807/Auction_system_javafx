package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Ensures {@code bid_transactions} exists for {@link com.auction.server.dao.auction.BidDao} and trending queries. */
final class BidTransactionsSchemaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidTransactionsSchemaMigration.class);

  private BidTransactionsSchemaMigration() {}

  static void apply(Connection conn) {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
          "CREATE TABLE IF NOT EXISTS bid_transactions ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "itemid INT NOT NULL, "
              + "userid INT NOT NULL, "
              + "bidvalue DOUBLE NOT NULL, "
              + "`timestamp` DATETIME NOT NULL, "
              + "version INT NOT NULL DEFAULT 0, "
              + "INDEX idx_bid_transactions_item (itemid), "
              + "INDEX idx_bid_transactions_user (userid), "
              + "INDEX idx_bid_transactions_item_time (itemid, `timestamp`)"
              + ")");
      MigrationSchemaSupport.addColumnIfMissing(
          conn, "bid_transactions", "version", "INT NOT NULL DEFAULT 0");
    } catch (Exception e) {
      LOGGER.warn("Failed to ensure bid_transactions table", e);
    }
  }
}
