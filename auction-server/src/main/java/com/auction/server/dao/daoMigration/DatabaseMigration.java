package com.auction.server.dao.daoMigration;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseMigration {
  public static boolean runAll(Connection connection) throws SQLException {
    UserMigration.migrate(connection);
    UserMigration.migratePasswordToSha256(connection);
    ItemMigration.migrate(connection);
    return true;
  }
}