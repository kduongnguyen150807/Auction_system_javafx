package com.auction.server.service.auction;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;

final class AutoBidRegistration {
  final int userId;
  final double maxAutoBid;
  final double increment;
  final LocalDateTime registrationTime;

  AutoBidRegistration(int userId, double maxAutoBid, double increment, LocalDateTime registrationTime) {
    this.userId = userId;
    this.maxAutoBid = maxAutoBid;
    this.increment = increment;
    this.registrationTime = registrationTime;
  }

  static PriorityQueue<AutoBidRegistration> newQueue() {
    return new PriorityQueue<>(
        Comparator.comparingDouble((AutoBidRegistration r) -> -r.maxAutoBid)
            .thenComparing(r -> r.registrationTime));
  }
}
