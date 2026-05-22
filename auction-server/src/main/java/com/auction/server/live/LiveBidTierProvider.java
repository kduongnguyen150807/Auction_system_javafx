package com.auction.server.live;

import com.auction.shared.LiveBidTiers;

public final class LiveBidTierProvider implements BidTierProvider {
  @Override
  public LiveBidTiers compute(double currentPrice) {
    double base = Math.max(currentPrice, 0);
    double step1 = Math.max(base * 0.05, 1_000);
    double step2 = Math.max(base * 0.10, 5_000);
    double step3 = Math.max(base * 0.20, 10_000);
    return new LiveBidTiers(base, base + step1, base + step2, base + step3);
  }
}
