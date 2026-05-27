package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared DDL helpers for incremental schema setup. */
final class MigrationSchemaSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(MigrationSchemaSupport.class);

  private MigrationSchemaSupport() {}

  /**
   * Renames a column when an older schema used a different name (e.g. {@code seller_id} → {@code sellerid}).
   * Tries MySQL 8 {@code RENAME COLUMN}, then falls back to {@code CHANGE COLUMN} with {@code definition}.
   */
  static void renameColumnIfExists(
      Connection conn, String table, String fromColumn, String toColumn, String changeDefinition) {
    try {
      if (!columnExists(conn, table, fromColumn) || columnExists(conn, table, toColumn)) {
        return;
      }
      try (Statement st = conn.createStatement()) {
        st.execute(
            "ALTER TABLE "
                + table
                + " RENAME COLUMN "
                + fromColumn
                + " TO "
                + toColumn);
        LOGGER.info("Renamed column {}.{} -> {}", table, fromColumn, toColumn);
      }
    } catch (SQLException renameFailed) {
      try {
        if (columnExists(conn, table, fromColumn) && !columnExists(conn, table, toColumn)) {
          try (Statement st = conn.createStatement()) {
            st.execute(
                "ALTER TABLE "
                    + table
                    + " CHANGE COLUMN `"
                    + fromColumn
                    + "` `"
                    + toColumn
                    + "` "
                    + changeDefinition);
            LOGGER.info("Renamed column {}.{} -> {} (CHANGE)", table, fromColumn, toColumn);
          }
        }
      } catch (SQLException e) {
        LOGGER.warn("Failed to rename column {}.{} -> {}", table, fromColumn, toColumn, e);
      }
    }
  }

  static void addColumnIfMissing(Connection conn, String table, String column, String definition) {
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

  static void executeIfColumnAdded(Connection conn, String table, String column, String sql) {
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

  static void createUniqueIndexIfMissing(Connection conn, String table, String indexName, String column) {
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

  static boolean columnExists(Connection conn, String table, String column) throws SQLException {
    String sql =
        "SELECT 1 FROM information_schema.columns "
            + "WHERE table_schema = database() AND table_name = ? AND column_name = ? LIMIT 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, table);
      ps.setString(2, column);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  static boolean indexExists(Connection conn, String table, String indexName) throws SQLException {
    String sql =
        "SELECT 1 FROM information_schema.statistics "
            + "WHERE table_schema = database() AND table_name = ? AND index_name = ? LIMIT 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, table);
      ps.setString(2, indexName);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }
}
