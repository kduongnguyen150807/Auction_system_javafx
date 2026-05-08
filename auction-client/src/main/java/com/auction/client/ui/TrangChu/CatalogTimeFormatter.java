package com.auction.client.ui.TrangChu;

import com.auction.shared.AuctionType;
import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Item;
import java.time.Duration;
import java.time.LocalDateTime;

/** Remaining time labels for auction cards. */
public final class CatalogTimeFormatter {

  private CatalogTimeFormatter() {}

  /** Countdown toward auction end (English) or next drop / end (Dutch). */
  public static String formatRemainingForItem(Item item) {
    if (item == null) {
      return "N/A";
    }
    LocalDateTime now = LocalDateTime.now();
    if (item.getAuctionType() == AuctionType.DUTCH) {
      LocalDateTime target = DutchAuctionPricing.countdownTarget(item, now);
      return DutchAuctionPricing.formatShortCountdownToward(target, now);
    }
    return formatRemaining(item.getEndTime());
  }

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
