package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RatingsSchemaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(RatingsSchemaMigration.class);

  private RatingsSchemaMigration() {}

  static void apply(Connection conn) {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
          "CREATE TABLE IF NOT EXISTS ratings ("
              + "id INT AUTO_INCREMENT PRIMARY KEY, "
              + "itemid INT NOT NULL, "
              + "rateruserid INT NOT NULL, "
              + "rateduserid INT NOT NULL, "
              + "stars INT NOT NULL, "
              + "feedback TEXT, "
              + "createdat DATETIME DEFAULT CURRENT_TIMESTAMP, "
              + "UNIQUE KEY uq_rating (itemid, rateruserid))");
    } catch (Exception e) {
      LOGGER.warn("Failed to ensure ratings table", e);
    }
  }
}
