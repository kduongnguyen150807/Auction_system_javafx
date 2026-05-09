package com.auction.shared;

import java.util.Locale;

public enum ItemStatus {
  /** Awaiting admin approval before going live. */
  PENDING,
  /** Approved and accepting bids. */
  OPEN,
  /** Closed via Buy-It-Now. */
  CLOSED,
  /** Time expired — winner determined, awaiting manual settlement confirmation. */
  FINISHED,
<<<<<<< HEAD
  PAID,
  CANCELED,
  EXPRIED,
=======
  /** Ended with no winning bidder when the auction time elapsed. */
  EXPIRED,
  /** Cancelled (e.g. seller banned, or admin rejection). */
  CANCELED;

  /**
   * Maps a persisted {@code items.status} string (case-insensitive). Blank or unknown values
   * fall back to {@link #OPEN} so row mapping never fails on legacy or typo values.
   */
  public static ItemStatus parse(String raw) {
    if (raw == null || raw.isBlank()) return OPEN;
    try {
      return valueOf(raw.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ignored) {
      return OPEN;
    }
  }
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068
}
