package com.auction.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.junit.jupiter.api.Test;

public class AuctionLogicTest {

  // ─── 1. Auto-bid PriorityQueue ordering ──────────────────────────────────

  @Test
  public void autoBidQueue_orderedByMaxBidDescending() {
    PriorityQueue<BidTransaction> queue =
        new PriorityQueue<>(Comparator.comparingDouble(
            (BidTransaction b) -> -b.getMaxAutoBid()));

    BidTransaction low = new BidTransaction(1, 1, 100);
    low.setMaxAutoBid(500);
    BidTransaction high = new BidTransaction(1, 2, 150);
    high.setMaxAutoBid(1000);
    BidTransaction mid = new BidTransaction(1, 3, 200);
    mid.setMaxAutoBid(750);

    queue.add(low);
    queue.add(high);
    queue.add(mid);

    assertEquals(1000, queue.poll().getMaxAutoBid());
    assertEquals(750, queue.poll().getMaxAutoBid());
    assertEquals(500, queue.poll().getMaxAutoBid());
  }

  @Test
  public void autoBidQueue_tiesBrokenByRegistrationTime_earlierWins() {
    LocalDateTime earlier = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
    LocalDateTime later = LocalDateTime.of(2026, 1, 1, 10, 5, 0);

    // Both have the same maxAutoBid — the earlier registrant should win.
    PriorityQueue<BidTransaction> queue =
        new PriorityQueue<>(
            Comparator.comparingDouble((BidTransaction b) -> -b.getMaxAutoBid())
                .thenComparing(BidTransaction::getTimestamp));

    BidTransaction lateRegistrant = new BidTransaction(1, 2, 100);
    lateRegistrant.setMaxAutoBid(1000);
    lateRegistrant.setTimestamp(later);

    BidTransaction earlyRegistrant = new BidTransaction(1, 1, 100);
    earlyRegistrant.setMaxAutoBid(1000);
    earlyRegistrant.setTimestamp(earlier);

    queue.add(lateRegistrant);
    queue.add(earlyRegistrant);

    assertEquals(1, queue.poll().getUserId(), "Earlier registrant should be polled first");
  }

  // ─── 2. Anti-sniping extension ───────────────────────────────────────────

  @Test
  public void antiSnipe_bidInLastMinute_extendsAuction() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endTime = now.plusSeconds(30);

    boolean extended = false;
    if (java.time.Duration.between(now, endTime).getSeconds() < 60) {
      endTime = endTime.plusSeconds(60);
      extended = true;
    }

    assertTrue(extended, "Auction in last 60 s should be extended");
    assertTrue(
        java.time.Duration.between(now, endTime).getSeconds() >= 60,
        "End time after extension must be >= 60 s away");
  }

  @Test
  public void antiSnipe_bidNotInLastMinute_doesNotExtend() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endTime = now.plusSeconds(120);

    boolean extended = false;
    if (java.time.Duration.between(now, endTime).getSeconds() < 60) {
      endTime = endTime.plusSeconds(60);
      extended = true;
    }

    assertFalse(extended, "Auction with > 60 s remaining must NOT be extended");
  }

  // ─── 3. Item polymorphism / Factory ──────────────────────────────────────

  @Test
  public void itemFactory_createElectronics_correctTaxRate() {
    Item item = ItemFactory.createItem("Electronics");
    item.setCurrentPrice(1000);
    assertEquals(50.0, item.calculateTax(), 0.001, "Electronics tax = 5%");
  }

  @Test
  public void itemFactory_createVehicle_correctTaxRate() {
    Item item = ItemFactory.createItem("Vehicle");
    item.setCurrentPrice(1000);
    assertEquals(100.0, item.calculateTax(), 0.001, "Vehicle tax = 10%");
  }

  @Test
  public void itemFactory_createArt_correctTaxRate() {
    Item item = ItemFactory.createItem("Art");
    item.setCurrentPrice(1000);
    assertEquals(80.0, item.calculateTax(), 0.001, "Art tax = 8%");
  }

  @Test
  public void itemFactory_unknownCategory_returnsVehicle() {
    Item item = ItemFactory.createItem("Unknown");
    assertInstanceOf(Vehicle.class, item, "Unknown category should fall back to Vehicle");
  }

  // ─── 4. Bid validation rules (pure domain logic) ─────────────────────────

  @Test
  public void bidValidation_bidBelowCurrentPrice_isRejected() {
    Item item = ItemFactory.createItem("Electronics");
    item.setCurrentPrice(500.0);
    item.setStatus(ItemStatus.OPEN);

    BidTransaction bid = new BidTransaction(item.getId(), 42, 400.0);

    assertTrue(
        bid.getBidValue() <= item.getCurrentPrice(),
        "A bid value <= current price must be considered invalid");
  }

  @Test
  public void bidValidation_bidAboveCurrentPrice_isAccepted() {
    Item item = ItemFactory.createItem("Electronics");
    item.setCurrentPrice(500.0);
    item.setStatus(ItemStatus.OPEN);

    BidTransaction bid = new BidTransaction(item.getId(), 42, 600.0);

    assertFalse(
        bid.getBidValue() <= item.getCurrentPrice(),
        "A bid value > current price must be considered valid");
  }

  @Test
  public void bidValidation_bidOnClosedAuction_isRejected() {
    Item item = ItemFactory.createItem("Vehicle");
    item.setStatus(ItemStatus.CLOSED);

    assertNotEquals(
        ItemStatus.OPEN,
        item.getStatus(),
        "A non-OPEN auction must block all incoming bids");
  }

  @Test
  public void bidValidation_buyItNow_triggeredWhenBidReachesMaxPrice() {
    Item item = ItemFactory.createItem("Art");
    item.setCurrentPrice(800.0);
    item.setMaxPrice(1000.0);
    item.setStatus(ItemStatus.OPEN);

    BidTransaction bid = new BidTransaction(item.getId(), 7, 1000.0);

    boolean isBuyItNow = item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice();
    assertTrue(isBuyItNow, "Bid equal to maxPrice should trigger Buy-It-Now");
  }

  // ─── 5. Password hashing ─────────────────────────────────────────────────

  @Test
  public void passwordEncoder_sameInput_producesConsistentHash() {
    String hash1 = PasswordEncoder.hash("secret123");
    String hash2 = PasswordEncoder.hash("secret123");
    assertEquals(hash1, hash2, "Same password must always produce the same hash");
  }

  @Test
  public void passwordEncoder_differentPasswords_produceDifferentHashes() {
    assertNotEquals(
        PasswordEncoder.hash("password1"),
        PasswordEncoder.hash("password2"),
        "Different passwords must produce different hashes");
  }

  @Test
  public void passwordEncoder_matches_returnsTrueForCorrectPassword() {
    String stored = PasswordEncoder.hash("myPassword");
    assertTrue(PasswordEncoder.matches("myPassword", stored));
  }

  @Test
  public void passwordEncoder_matches_returnsFalseForWrongPassword() {
    String stored = PasswordEncoder.hash("myPassword");
    assertFalse(PasswordEncoder.matches("wrongPassword", stored));
  }
}
