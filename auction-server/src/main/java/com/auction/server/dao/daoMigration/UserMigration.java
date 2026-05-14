package com.auction.server.dao.daoMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserMigration {

  public static void migrate(Connection conn) throws SQLException {
    MigrationSchemaSupport.createTableIfMissing(
      conn,
      "users",
      """
        CREATE TABLE IF NOT EXISTS users (
          id INT PRIMARY KEY AUTO_INCREMENT,
          fullname VARCHAR(255) NULL,
          username VARCHAR(255) NOT NULL,
          password VARCHAR(255) NOT NULL,
          balance  DOUBLE NOT NULL DEFAULT 0,
          age INTEGER NOT NULL DEFAULT 0,
          email VARCHAR(255) NOT NULL,
          role VARCHAR(255) NOT NULL DEFAULT 'BIDDER',
          phonenumber VARCHAR(255),
          status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
          avatar VARCHAR(255),
          isactive BOOLEAN DEFAULT true)"""
    );

    MigrationSchemaSupport.addColumnIfMissing(
      conn,
      "users",
      "age",
      "INTEGER NOT NULL DEFAULT 0"
    );
  }

  public static void migratePasswordToSha256(Connection conn) throws SQLException {
    String sql = "UPDATE users " +
      "SET password = LOWER(SHA2(password, 256)) " +
      "WHERE LENGTH(password) <> 64 " +
      "OR password REGEXP '[^0-9a-f]'";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      boolean res = ps.executeUpdate() > 0;
      if (res) {
        MigrationSchemaSupport.LOGGER.info("Password has been migrated");
      } else {
        MigrationSchemaSupport.LOGGER.info("Password has NOT been migrated");
      }
    }
  }
}
