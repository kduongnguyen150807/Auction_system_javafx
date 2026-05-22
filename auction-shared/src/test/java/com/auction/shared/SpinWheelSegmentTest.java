package com.auction.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SpinWheelSegmentTest {

  @Test
  void weights_sumTo100k_withRareJackpotsAt001Percent() {
    int total = 0;
    for (SpinWheelSegment segment : SpinWheelSegment.values()) {
      total += segment.getWeight();
    }
    assertEquals(100_000, total);
    assertEquals(10, SpinWheelSegment.VIP_1_YEAR.getWeight());
    assertEquals(10, SpinWheelSegment.CASH_10000.getWeight());
    assertTrue(SpinWheelSegment.BETTER_LUCK.getWeight() > SpinWheelSegment.VIP_1_DAY.getWeight());
  }

  @Test
  void roll_returnsValidSegment() {
    SpinWheelSegment prize = SpinWheelSegment.roll(new java.util.Random(42));
    assertNotNull(prize);
    assertTrue(prize.getIndex() >= 0 && prize.getIndex() < 7);
  }
}
