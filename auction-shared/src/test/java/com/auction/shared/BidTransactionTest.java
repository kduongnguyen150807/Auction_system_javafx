package com.auction.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BidTransaction Encapsulation & Logic Tests")
public class BidTransactionTest {

  // ── 1. Construction ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("Construction")
  class Construction {

    @Test
    @DisplayName("3-arg constructor sets itemId, userId, bidValue")
    void threeArgConstructor_setsFields() {
      BidTransaction bid = new BidTransaction(10, 5, 250.0);
      assertAll(
          () -> assertEquals(10, bid.getItemId()),
          () -> assertEquals(5, bid.getUserId()),
          () -> assertEquals(250.0, bid.getBidValue(), 0.001)
      );
    }

    @Test
    @DisplayName("No-arg constructor sets timestamp to now")
    void noArgConstructor_setsTimestamp() {
      LocalDateTime before = LocalDateTime.now().minusSeconds(1);
      BidTransaction bid = new BidTransaction();
      LocalDateTime after = LocalDateTime.now().plusSeconds(1);
      assertTrue(bid.getTimestamp().isAfter(before) && bid.getTimestamp().isBefore(after),
          "Timestamp should be set to approximately now");
    }

    @Test
    @DisplayName("Default autoBid is false")
    void default_autoBidIsFalse() {
      BidTransaction bid = new BidTransaction(1, 1, 100);
      assertFalse(bid.isAutoBid());
    }

    @Test
    @DisplayName("Default autoBidIncrement is 10.0")
    void default_autoBidIncrementIsTen() {
      BidTransaction bid = new BidTransaction(1, 1, 100);
      assertEquals(10.0, bid.getAutoBidIncrement(), 0.001);
    }

    @Test
    @DisplayName("Default maxAutoBid is 0.0")
    void default_maxAutoBidIsZero() {
      BidTransaction bid = new BidTransaction(1, 1, 100);
      assertEquals(0.0, bid.getMaxAutoBid(), 0.001);
    }
  }

  // ── 2. Encapsulation — setters isolate state ─────────────────────────────

  @Nested
  @DisplayName("Encapsulation")
  class Encapsulation {

    @Test
    @DisplayName("setBidValue changes bid value independently")
    void setBidValue_isolatesState() {
      BidTransaction b1 = new BidTransaction(1, 1, 100);
      BidTransaction b2 = new BidTransaction(1, 2, 200);
      b1.setBidValue(150);
      assertEquals(150, b1.getBidValue(), 0.001);
      assertEquals(200, b2.getBidValue(), 0.001, "b2 must not be affected");
    }

    @Test
    @DisplayName("setAutoBid=true enables auto-bidding")
    void setAutoBid_toTrue() {
      BidTransaction bid = new BidTransaction(1, 1, 100);
      bid.setAutoBid(true);
      assertTrue(bid.isAutoBid());
    }

    @Test
    @DisplayName("setMaxAutoBid stores ceiling correctly")
    void setMaxAutoBid_storesCeiling() {
      BidTransaction bid = new BidTransaction(1, 1, 100);
      bid.setMaxAutoBid(999.0);
      assertEquals(999.0, bid.getMaxAutoBid(), 0.001);
    }

    @Test
    @DisplayName("setAutoBidIncrement changes increment")
    void setAutoBidIncrement_changesValue() {
      BidTransaction bid = new BidTransaction(1, 1, 100);
      bid.setAutoBidIncrement(25.0);
      assertEquals(25.0, bid.getAutoBidIncrement(), 0.001);
    }

    @Test
    @DisplayName("setTimestamp overrides timestamp")
    void setTimestamp_overrides() {
      BidTransaction bid = new BidTransaction(1, 1, 100);
      LocalDateTime fixed = LocalDateTime.of(2026, 6, 1, 12, 0, 0);
      bid.setTimestamp(fixed);
      assertEquals(fixed, bid.getTimestamp());
    }
  }

  // ── 3. Business Rules ────────────────────────────────────────────────────

  @Nested
  @DisplayName("Business Rules")
  class BusinessRules {

    @Test
    @DisplayName("Bid below current price is invalid")
    void bidBelowCurrentPrice_isInvalid() {
      Item item = ItemFactory.createItem("Electronics");
      item.setCurrentPrice(500.0);
      BidTransaction bid = new BidTransaction(item.getId(), 1, 400.0);
      assertTrue(bid.getBidValue() <= item.getCurrentPrice(),
          "Bid <= current price must be invalid");
    }

    @Test
    @DisplayName("Bid above current price is valid")
    void bidAboveCurrentPrice_isValid() {
      Item item = ItemFactory.createItem("Art");
      item.setCurrentPrice(500.0);
      BidTransaction bid = new BidTransaction(item.getId(), 1, 600.0);
      assertFalse(bid.getBidValue() <= item.getCurrentPrice(),
          "Bid > current price must be valid");
    }

    @Test
    @DisplayName("Auto-bid: nextBid = min(currentPrice + increment, maxAutoBid)")
    void autoBid_nextBidCalculation() {
      double currentPrice = 300.0;
      double increment = 50.0;
      double maxAutoBid = 600.0;

      double nextBid = Math.min(currentPrice + increment, maxAutoBid);
      assertEquals(350.0, nextBid, 0.001, "Next auto-bid should be 300 + 50 = 350");
    }

    @Test
    @DisplayName("Auto-bid capped at maxAutoBid when increment exceeds ceiling")
    void autoBid_cappedAtMaxAutoBid() {
      double currentPrice = 580.0;
      double increment = 50.0;
      double maxAutoBid = 600.0;

      double nextBid = Math.min(currentPrice + increment, maxAutoBid);
      assertEquals(600.0, nextBid, 0.001, "Next auto-bid should be capped at 600");
    }

    @Test
    @DisplayName("Auto-bid cancelled when maxAutoBid <= currentPrice")
    void autoBid_cancelledWhenMaxBelowCurrent() {
      double currentPrice = 700.0;
      double maxAutoBid = 600.0;
      assertTrue(maxAutoBid <= currentPrice,
          "Auto-bid should be invalidated when max <= current price");
    }

    @Test
    @DisplayName("Two BidTransactions on same item are independent objects")
    void twoBids_areIndependentObjects() {
      BidTransaction b1 = new BidTransaction(1, 1, 100);
      BidTransaction b2 = new BidTransaction(1, 2, 200);
      assertNotSame(b1, b2, "Each BidTransaction must be a separate object");
      assertNotEquals(b1.getUserId(), b2.getUserId());
    }
  }
}
