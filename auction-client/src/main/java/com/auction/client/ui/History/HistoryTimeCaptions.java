package com.auction.client.ui.History;

import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Item;
import java.time.LocalDateTime;

/** Builds time captions for history item cards. */
final class HistoryTimeCaptions {

  private HistoryTimeCaptions() {}

  static String ongoing(Item item) {
    return buildHistoryCaption(item, true);
  }

  static String scheduled(Item item) {
    return buildHistoryCaption(item, false);
  }

  static String upcoming(Item item) {
    if (item.getWinnerUsername() != null && !item.getWinnerUsername().isEmpty()) {
      return "Winner: " + item.getWinnerUsername();
    }
    LocalDateTime start = item.getStartTime();
    if (start == null) {
      return "N/A";
    }
    return "Starts in " + formatCountdownToward(start);
  }

  private static String buildHistoryCaption(Item item, boolean ongoing) {
    String timeLabel =
        formatCountdownToward(ongoing ? item.getEndTime() : item.getStartTime());
    if (item.getWinnerUsername() != null && !item.getWinnerUsername().isEmpty()) {
      timeLabel = "Winner: " + item.getWinnerUsername();
    }
    return timeLabel;
  }

  private static String formatCountdownToward(LocalDateTime time) {
    if (time == null) {
      return "N/A";
    }
    LocalDateTime now = LocalDateTime.now();
    if (!time.isAfter(now)) {
      return "closed";
    }
    return DutchAuctionPricing.formatShortCountdownToward(time, now);
  }
}
