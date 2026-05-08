package com.auction.shared;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Shared countdown + price formulas for Dutch (descending) auctions. */
public final class DutchAuctionPricing {

  private static final double EPS = 1e-6;

  private DutchAuctionPricing() {}

  /** Minimum number of downward ticks needed to reach reserve from the starting ceiling. */
  public static long maxDropTicks(Item item) {
    if (item == null || item.getAuctionType() != AuctionType.DUTCH) return 0;
    double p0 = item.getStartingPrice();
    double reserve = Math.max(item.getDutchReservePrice(), 0);
    double tick = Math.max(item.getDutchTickAmount(), 0);
    if (tick <= EPS || p0 <= reserve + EPS) return 0;
    return (long) Math.ceil((p0 - reserve - EPS) / tick);
  }

  /**
   * Drops happen at fixed clock steps: {@code start + n * interval} minutes, for n = 1..Kmax. Each drop
   * decreases the listed price by one tick amount, never below reserve.
   */
  public static double computeEffectivePrice(Item item, LocalDateTime now) {
    if (item == null || item.getAuctionType() != AuctionType.DUTCH) {
      return item != null ? item.getCurrentPrice() : 0;
    }
    double p0 = item.getStartingPrice();
    double reserve = Math.max(item.getDutchReservePrice(), 0);
    double tick = Math.max(item.getDutchTickAmount(), 0);
    long intervalMinutes = Math.max(item.getDutchTickIntervalMinutes(), 0);
    LocalDateTime start = item.getStartTime();
    if (start == null || intervalMinutes <= 0 || tick <= EPS) return item.getCurrentPrice();

    LocalDateTime t = now.isBefore(start) ? start : now;
    long elapsedMinutes = Math.max(0, ChronoUnit.MINUTES.between(start, t));
    long ticksDone = elapsedMinutes / intervalMinutes;
    long kmax = maxDropTicks(item);
    long applied = Math.min(kmax, ticksDone);
    return Math.max(reserve, p0 - applied * tick);
  }

  /**
   * Moment the UI counts down toward: next planned price drop before reserve is reached and before the
   * auction ends, otherwise the auction end time.
   */
  public static LocalDateTime countdownTarget(Item item, LocalDateTime now) {
    LocalDateTime end = item != null ? item.getEndTime() : null;
    if (item == null || item.getAuctionType() != AuctionType.DUTCH) return end;

    LocalDateTime start = item.getStartTime();
    long intervalMinutes = Math.max(item.getDutchTickIntervalMinutes(), 0);
    if (start == null || intervalMinutes <= 0) return end != null ? end : now.plusDays(3650);

    long kmax = maxDropTicks(item);
    if (kmax <= 0) return end != null ? end : now.plusDays(3650);

    LocalDateTime tBase = now.isBefore(start) ? start : now;
    long elapsed = Math.max(0, ChronoUnit.MINUTES.between(start, tBase));
    long ticksApplied = elapsed / intervalMinutes;
    ticksApplied = Math.min(ticksApplied, kmax);

    if (ticksApplied >= kmax) {
      return end != null ? end : now.plusDays(3650);
    }

    LocalDateTime nextDrop = start.plusMinutes((ticksApplied + 1L) * intervalMinutes);
    if (end != null && nextDrop.isAfter(end)) {
      return end;
    }
    return nextDrop;
  }

  public static String formatShortCountdownToward(LocalDateTime target, LocalDateTime now) {
    if (target == null) return "N/A";
    Duration rem = Duration.between(now, target);
    if (rem.isNegative() || rem.isZero()) return "0s";
    long s = rem.getSeconds();
    long days = s / 86400;
    long hours = (s % 86400) / 3600;
    long mins = (s % 3600) / 60;
    long secs = s % 60;
    if (days > 0) return days + "d " + hours + "h";
    if (hours > 0) return hours + "h " + mins + "m";
    return mins + "m " + secs + "s";
  }
}
