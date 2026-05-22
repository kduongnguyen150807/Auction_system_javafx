package com.auction.server.live;

import com.auction.shared.LiveBidTiers;

/** Computes quick-bid tier amounts for live auctions (OCP: swappable provider). */
public interface BidTierProvider {
  LiveBidTiers compute(double currentPrice);
}
