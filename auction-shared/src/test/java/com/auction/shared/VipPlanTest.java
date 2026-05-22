package com.auction.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class VipPlanTest {

  @Test
  void fromId_resolvesKnownPlans() {
    assertEquals(VipPlan.DAYS_7, VipPlan.fromId("7d"));
    assertEquals(VipPlan.MONTH_1, VipPlan.fromId("1M"));
    assertEquals(VipPlan.YEAR_1, VipPlan.fromId("1y"));
    assertNull(VipPlan.fromId("unknown"));
  }

  @Test
  void user_isVip_whenExpiryInFuture() {
    User user = new Bidder();
    user.setVipUntil(LocalDateTime.now().plusDays(1));
    assertTrue(user.isVip());

    user.setVipUntil(LocalDateTime.now().minusMinutes(1));
    assertFalse(user.isVip());
  }
}
