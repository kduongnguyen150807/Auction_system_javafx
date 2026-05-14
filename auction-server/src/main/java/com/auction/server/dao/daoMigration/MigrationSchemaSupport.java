package com.auction.server.dao.daoMigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class MigrationSchemaSupport {
  public static final Logger LOGGER = LoggerFactory.getLogger(MigrationSchemaSupport.class);

  public static void createTableIfMissing(
    Connection conn,
    String tableName,
    String createSql
  ) throws SQLException {
    DatabaseMetaData metaData = conn.getMetaData();
    try (
      ResultSet rs = metaData.getTables(
        null, null, tableName, new String[]{"TABLES"});
    ) {
      if (!rs.next()) {
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(createSql);
        }
      }

      LOGGER.info(String.format("Created table %s", tableName));
    }
  }

  public static void addColumnIfMissing(
    Connection conn,
    String tableName,
    String columnName,
    String columnDefinition
  ) throws SQLException {
    DatabaseMetaData metaData = conn.getMetaData();
    try (
      ResultSet rs = metaData.getColumns(null, null, tableName, columnName);
      ) {
      if (!rs.next()) {
        String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(sql);
        }

        LOGGER.info(String.format("Added column %s", columnName));
      }
    }
  }

  public static void createIndexIfMissing(
    Connection conn,
    String tableName,
    String indexName,
    String indexSql
  ) throws SQLException {
    DatabaseMetaData metaData = conn.getMetaData();

    try (
      ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, false);
      ) {
      while (rs.next()) {
        String existing = rs.getString("INDEX_NAME");
        if (indexName.equalsIgnoreCase(existing)) {
          return;
        }
      }
    }

    try (Statement stmt = conn.createStatement()) {
      stmt.execute(indexSql);
    }

    LOGGER.info(String.format("Created index %s", indexName));
  }
}
