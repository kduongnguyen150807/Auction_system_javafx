package com.auction.server.dao.platform;

import java.sql.Connection;

final class ItemSchemaMigration {

  private ItemSchemaMigration() {}

  static void apply(Connection conn) {
    MigrationSchemaSupport.addColumnIfMissing(conn, "items", "image_url", "VARCHAR(2048) NULL");
    MigrationSchemaSupport.addColumnIfMissing(conn, "items", "maxprice", "DOUBLE NULL");
  }
}
