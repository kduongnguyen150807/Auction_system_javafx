package com.auction.shared;

public enum ItemStatus {
  /** Awaiting admin approval before going live. */
  PENDING,
  /** Approved and accepting bids. */
  OPEN,
  /** Closed via Buy-It-Now. */
  CLOSED,
  /** Time expired — winner determined, awaiting manual settlement confirmation. */
  FINISHED,
  /** Cancelled (e.g. seller banned, or admin rejection). */
  CANCELED
}
