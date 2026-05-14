package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ItemSchemaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemSchemaMigration.class);

  private ItemSchemaMigration() {}

  static void apply(Connection conn) {
    ensureItemsTable(conn);
    MigrationSchemaSupport.addColumnIfMissing(conn, "items", "image_url", "VARCHAR(2048) NULL");
    MigrationSchemaSupport.addColumnIfMissing(conn, "items", "maxprice", "DOUBLE NULL");
    // Dutch auction catalog fields (additive; existing rows stay ENGLISH)
    MigrationSchemaSupport.addColumnIfMissing(
        conn,
        "items",
        "auction_type",
        "VARCHAR(20) NOT NULL DEFAULT 'ENGLISH'");
    MigrationSchemaSupport.addColumnIfMissing(conn, "items", "dutch_reserve_price", "DOUBLE NULL");
    MigrationSchemaSupport.addColumnIfMissing(conn, "items", "dutch_tick_amount", "DOUBLE NULL");
    MigrationSchemaSupport.addColumnIfMissing(conn, "items", "dutch_tick_interval_mins", "INT NULL");
  }

  private static void ensureItemsTable(Connection conn) {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
          "CREATE TABLE IF NOT EXISTS items ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "category VARCHAR(100) NOT NULL DEFAULT 'Vehicle', "
              + "auction_type VARCHAR(20) NOT NULL DEFAULT 'ENGLISH', "
              + "name VARCHAR(255) NOT NULL, "
              + "description TEXT, "
              + "startingprice DOUBLE NOT NULL DEFAULT 0, "
              + "currentprice DOUBLE NOT NULL DEFAULT 0, "
              + "maxprice DOUBLE NULL, "
              + "dutch_reserve_price DOUBLE NULL, "
              + "dutch_tick_amount DOUBLE NULL, "
              + "dutch_tick_interval_mins INT NULL, "
              + "starttime DATETIME NULL, "
              + "endtime DATETIME NULL, "
              + "sellerid INT NOT NULL, "
              + "winnerid INT NULL, "
              + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', "
              + "version INT NOT NULL DEFAULT 0, "
              + "image_url VARCHAR(2048) NULL, "
              + "INDEX idx_items_seller (sellerid), "
              + "INDEX idx_items_status_end (status, endtime)"
              + ")");
    } catch (Exception e) {
      LOGGER.warn("Failed to ensure items table", e);
    }
  }
}
