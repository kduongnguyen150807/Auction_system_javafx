package com.auction.server.dao.platform;

import java.sql.Connection;

final class ItemSchemaMigration {

  private ItemSchemaMigration() {}

  static void apply(Connection conn) {
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
}
