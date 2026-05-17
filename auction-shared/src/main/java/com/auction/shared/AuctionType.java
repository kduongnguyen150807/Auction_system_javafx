package com.auction.shared;

/** Canonical auction mechanism for a listing. */
public enum AuctionType {
  ENGLISH,
  DUTCH;

  public static AuctionType parse(String raw) {
    if (raw == null || raw.isBlank()) return ENGLISH;
    try {
      return valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
    } catch (IllegalArgumentException e) {
      return ENGLISH;
    }
  }

  /** Database / wire label (uppercase ASCII). */
  public String dbName() {
    return name();
  }

  @Override
  public String toString() {
    return name();
  }
}
