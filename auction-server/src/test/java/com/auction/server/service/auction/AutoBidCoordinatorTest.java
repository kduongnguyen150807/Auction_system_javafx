package com.auction.server.service.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AutoBidCoordinator")
class AutoBidCoordinatorTest {

  private static AutoBidRegistration reg(int userId, double max, double inc) {
    return new AutoBidRegistration(userId, max, inc, LocalDateTime.now());
  }

  @Test
  @DisplayName("Single proxy counters manual bid at current + increment")
  void computeCounterTarget_singleProxy() {
    AutoBidRegistration top = reg(10, 1000, 50);
    assertEquals(650, AutoBidCoordinator.computeCounterTarget(600, top, null), 0.001);
  }

  @Test
  @DisplayName("Single proxy capped at maxAutoBid")
  void computeCounterTarget_singleProxy_capped() {
    AutoBidRegistration top = reg(10, 620, 50);
    assertEquals(620, AutoBidCoordinator.computeCounterTarget(600, top, null), 0.001);
  }

  @Test
  @DisplayName("Two proxies use max of proxy formula and beat-current")
  void computeCounterTarget_twoProxies() {
    AutoBidRegistration top = reg(10, 1000, 50);
    AutoBidRegistration second = reg(20, 800, 50);
    assertEquals(850, AutoBidCoordinator.computeCounterTarget(600, top, second), 0.001);
  }

  @Test
  @DisplayName("Two proxies still beat high manual bid when proxy formula is too low")
  void computeCounterTarget_twoProxies_manualOutbid() {
    AutoBidRegistration top = reg(10, 1000, 50);
    AutoBidRegistration second = reg(20, 800, 50);
    assertEquals(950, AutoBidCoordinator.computeCounterTarget(900, top, second), 0.001);
  }
}
