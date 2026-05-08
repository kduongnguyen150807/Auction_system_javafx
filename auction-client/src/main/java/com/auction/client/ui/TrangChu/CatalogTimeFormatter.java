package com.auction.client.ui.TrangChu;

import java.time.Duration;
import java.time.LocalDateTime;

/** Remaining time labels for auction cards. */
public final class CatalogTimeFormatter {

  private CatalogTimeFormatter() {}

  public static String formatRemaining(LocalDateTime endTime) {
    if (endTime == null) {
      return "N/A";
    }
    Duration remaining = Duration.between(LocalDateTime.now(), endTime);
    if (remaining.isNegative() || remaining.isZero()) {
      return "closed";
    }
    long hours = remaining.toHours();
    if (hours / 24 > 0) {
      return (hours / 24) + "d " + (hours % 24) + "h";
    }
    return (hours % 24) + "h " + (remaining.toMinutes() % 60) + "m";
  }
}
