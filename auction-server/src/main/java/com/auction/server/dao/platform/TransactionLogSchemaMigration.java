package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TransactionLogSchemaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogSchemaMigration.class);

  private TransactionLogSchemaMigration() {}

  static void apply(Connection conn) {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
          "CREATE TABLE IF NOT EXISTS transaction_logs ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "userid INT NOT NULL, "
              + "type VARCHAR(50) NOT NULL, "
              + "amount DOUBLE NOT NULL, "
              + "itemid INT DEFAULT 0, "
              + "createdat DATETIME DEFAULT CURRENT_TIMESTAMP)");
      MigrationSchemaSupport.createIndexIfMissing(
              conn, "transaction_logs", "idx_transaction_user_time", "userid, createdat");

      MigrationSchemaSupport.createIndexIfMissing(
              conn, "transaction_logs", "idx_transaction_item", "itemid");

      MigrationSchemaSupport.createIndexIfMissing(
              conn, "transaction_logs", "idx_transaction_type", "type");
    } catch (Exception e) {
      LOGGER.warn("Failed to ensure transaction_logs table", e);
    }
  }
}
