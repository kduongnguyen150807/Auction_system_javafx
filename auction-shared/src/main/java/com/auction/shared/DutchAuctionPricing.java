package com.auction.shared;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Shared countdown + price formulas for Dutch (descending) auctions. */
public final class DutchAuctionPricing {

  private static final double EPS = 1e-6;
  private static final double MONEY_TOLERANCE = 0.02;

  private DutchAuctionPricing() {}

  /** Rounds a dollar amount to two decimal places (cents). */
  public static double roundMoney(double amount) {
    return Math.round(amount * 100.0) / 100.0;
  }

  /** UI label for a Dutch listed price, always two decimals. */
  public static String formatListedPrice(double amount) {
    return String.format("%,.2f$", roundMoney(amount));
  }

  /** Compare bid amount to listed Dutch price (both rounded to cents). */
  public static boolean bidMatchesListedPrice(double bidAmount, double listedPrice) {
    return Math.abs(roundMoney(bidAmount) - roundMoney(listedPrice)) <= MONEY_TOLERANCE;
  }

  /** Minimum number of downward ticks needed to reach reserve from the starting ceiling. */
  public static long maxDropTicks(Item item) {
    if (item == null || item.getAuctionType() != AuctionType.DUTCH) return 0;
    return maxDropTicks(item.getStartingPrice(), item.getDutchReservePrice(), item.getDutchTickAmount());
  }

  /** Same as {@link #maxDropTicks(Item)} from raw Dutch parameters. */
  public static long maxDropTicks(double startingPrice, double reserve, double tick) {
    double p0 = startingPrice;
    double floor = Math.max(reserve, 0);
    double step = Math.max(tick, 0);
    if (step <= EPS || p0 <= floor + EPS) return 0;
    return (long) Math.ceil((p0 - floor - EPS) / step);
  }

  /**
   * Minimum minutes from {@code startTime} until the last scheduled drop at {@code intervalMinutes}.
   * Auction {@code endTime} must be at or after {@code start + this duration}.
   */
  public static long minAuctionDurationMinutes(
      double startingPrice, double reserve, double tick, int intervalMinutes) {
    long kmax = maxDropTicks(startingPrice, reserve, tick);
    if (kmax <= 0 || intervalMinutes <= 0) return 0;
    return kmax * intervalMinutes;
  }

  public static LocalDateTime suggestedEndTime(
      LocalDateTime start, double startingPrice, double reserve, double tick, int intervalMinutes) {
    if (start == null) return null;
    long mins = minAuctionDurationMinutes(startingPrice, reserve, tick, intervalMinutes);
    return mins > 0 ? start.plusMinutes(mins) : start;
  }

  /**
   * Number of drop slots between {@code start} and {@code end} at {@code intervalMinutes} apart.
   */
  public static long dropSlotsBetween(LocalDateTime start, LocalDateTime end, int intervalMinutes) {
    if (start == null || end == null || !end.isAfter(start) || intervalMinutes <= 0) {
      return 0;
    }
    return ChronoUnit.MINUTES.between(start, end) / intervalMinutes;
  }

  /**
   * Tick amount so price moves evenly from {@code startingPrice} to {@code reserve} across all slots
   * between {@code start} and {@code end}. Returns {@code -1} when inputs are invalid.
   */
  public static double derivedTickAmount(
      LocalDateTime start,
      LocalDateTime end,
      int intervalMinutes,
      double startingPrice,
      double reserve) {
    if (reserve < 0 || reserve >= startingPrice) {
      return -1;
    }
    long slots = dropSlotsBetween(start, end, intervalMinutes);
    if (slots <= 0) {
      return -1;
    }
    double raw = (startingPrice - reserve) / slots;
    return Math.round(raw * 100.0) / 100.0;
  }

  /** Validates Dutch schedule when seller picks start, end, interval; tick is derived. */
  public static String validateDutchScheduleFromInterval(
      LocalDateTime start,
      LocalDateTime end,
      double startingPrice,
      double reserve,
      int intervalMinutes) {
    if (intervalMinutes <= 0) {
      return "Decrease interval must be at least 1 minute";
    }
    if (start == null || end == null || !end.isAfter(start)) {
      return "invalid_time_range";
    }
    double tick = derivedTickAmount(start, end, intervalMinutes, startingPrice, reserve);
    if (tick <= 0) {
      return "dutch_window_too_short";
    }
    return validateDutchSchedule(start, end, startingPrice, reserve, tick, intervalMinutes);
  }

  /** Validates Dutch numeric fields and that {@code end} allows every scheduled drop. */
  public static String validateDutchSchedule(
      LocalDateTime start,
      LocalDateTime end,
      double startingPrice,
      double reserve,
      double tick,
      int intervalMinutes) {
    if (reserve < 0) return "Invalid reserve price";
    if (reserve >= startingPrice) return "Reserve must be below starting price";
    if (tick <= 0) return "Price decrement must be positive";
    if (intervalMinutes <= 0) return "Decrease interval must be at least 1 minute";
    if (start == null || end == null || !end.isAfter(start)) return "invalid_time_range";
    long minMins = minAuctionDurationMinutes(startingPrice, reserve, tick, intervalMinutes);
    if (minMins > 0) {
      LocalDateTime minEnd = start.plusMinutes(minMins);
      if (end.isBefore(minEnd)) {
        return "end_time_too_early_for_dutch_drops";
      }
    }
    return null;
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
    return roundMoney(Math.max(reserve, p0 - applied * tick));
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
