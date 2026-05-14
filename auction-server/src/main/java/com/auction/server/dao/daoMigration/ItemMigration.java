package com.auction.server.dao.daoMigration;

import java.sql.Connection;
import java.sql.SQLException;

public class ItemMigration {
  public static void migrate(Connection conn) throws SQLException {
    MigrationSchemaSupport.createTableIfMissing(
      conn,
      "items",
      """
        CREATE TABLE IF NOT EXISTS items (
          id INT PRIMARY KEY AUTO_INCREMENT,
          version INT NOT NULL DEFAULT 0,
          category VARCHAR(255) NOT NULL DEFAULT 'VEHICLE',
          name VARCHAR(255) NOT NULL,
          description TEXT,
          startingprice DOUBLE NOT NULL,
          currentprice DOUBLE NOT NULL,
          maxprice DOUBLE NULL,
          starttime DATETIME NULL,
          endtime DATETIME NULL,
          sellerid INT NOT NULL,
          winnerid INT NULL,
          status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
          image_url VARCHAR(255) NULL,
          FOREIGN KEY (sellerid) REFERENCES users(id)
          )"""
    );
  }
}
