package com.auction.server.live;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.shared.LiveBidTiers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LiveBidTierProvider")
class LiveBidTierProviderTest {

  private final LiveBidTierProvider provider = new LiveBidTierProvider();

  @Test
  void computeFromZero_usesMinimumSteps() {
    LiveBidTiers tiers = provider.compute(0);
    assertEquals(0, tiers.getCurrentPrice());
    assertEquals(1_000, tiers.getTier1());
    assertEquals(5_000, tiers.getTier2());
    assertEquals(10_000, tiers.getTier3());
  }

  @Test
  void computeFromLargePrice_usesPercentageSteps() {
    LiveBidTiers tiers = provider.compute(100_000);
    assertEquals(100_000, tiers.getCurrentPrice());
    assertEquals(105_000, tiers.getTier1(), 0.01);
    assertEquals(110_000, tiers.getTier2(), 0.01);
    assertEquals(120_000, tiers.getTier3(), 0.01);
  }

  @Test
  void tiersAreStrictlyIncreasing() {
    LiveBidTiers tiers = provider.compute(50_000);
    assertTrue(tiers.getTier1() > tiers.getCurrentPrice());
    assertTrue(tiers.getTier2() > tiers.getTier1());
    assertTrue(tiers.getTier3() > tiers.getTier2());
  }
}
