package com.auction.server.service.auction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrendingLotsFormulaTest {

  @Test
  @DisplayName("Higher short-window mass (burst) yields higher score than flat activity at same W_long")
  void burstBeatsEvenSpread() {
    double wLong = 4.0;
    double flatShort = 0.2;
    double burstShort = 3.5;
    double flat = TrendingLotsFormula.computeTrendScore(wLong, flatShort, 5);
    double burst = TrendingLotsFormula.computeTrendScore(wLong, burstShort, 5);
    assertTrue(burst > flat, "Burst component should boost ranking");
  }

  @Test
  @DisplayName("More distinct bidders increases score when other terms equal")
  void diversityBoosts() {
    double wLong = 3.0;
    double wShort = 0.5;
    double lowU = TrendingLotsFormula.computeTrendScore(wLong, wShort, 1);
    double highU = TrendingLotsFormula.computeTrendScore(wLong, wShort, 10);
    assertTrue(highU > lowU);
  }

  @Test
  @DisplayName("No bids (zero mass) gives zero score via log1p(0) branch")
  void zeroMassStaysFinite() {
    double s = TrendingLotsFormula.computeTrendScore(0, 0, 0);
    assertTrue(s >= 0 && s < TrendingLotsFormula.EPS);
    assertDoesNotThrow(() -> TrendingLotsFormula.computeTrendScore(0, 0, 100));
  }
}
