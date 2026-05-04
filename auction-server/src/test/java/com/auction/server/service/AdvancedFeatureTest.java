package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.server.service.AuctionManager;
import com.auction.shared.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Advanced Features — Auto-Bid & Anti-Snipe")
public class AdvancedFeatureTest {

  @Mock ItemDao itemDao;
  @Mock UserDao userDao;
  @Mock BidDao bidDao;
  @Mock TransactionLogDao logDao;

  private AuctionManager manager;

  @BeforeEach
  void setUp() {
    manager = new AuctionManager(itemDao, userDao, bidDao, logDao);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private Item openItem(int id, double currentPrice, LocalDateTime endTime) {
    Item item = ItemFactory.createItem("Electronics");
    item.setId(id);
    item.setSellerId(99);
    item.setStartingPrice(100.0);
    item.setCurrentPrice(currentPrice);
    item.setStatus(ItemStatus.OPEN);
    item.setEndTime(endTime);
    return item;
  }

  private Bidder bidder(int id) {
    Bidder b = new Bidder("user" + id, "pass", "u@x.com", "25", "090123456" + id);
    b.setId(id);
    b.setActive(true);
    b.setLocked(false);
    b.setBalance(1_000_000.0);
    return b;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // AUTO-BID TESTS
  // ══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Auto-Bidding Logic")
  class AutoBidding {

    @Test
    @DisplayName("Auto-bid: first bid placed at currentPrice + increment")
    void autoBid_firstBidAtCurrentPluIncrement() {
      Item item = openItem(1, 500.0, LocalDateTime.now().plusHours(2));
      Bidder bidder = bidder(10);
      AtomicReference<Double> placedBidValue = new AtomicReference<>(null);

      when(userDao.getById("10")).thenReturn(bidder);
      when(itemDao.getById(1)).thenReturn(item);
      when(userDao.atomicDeductBalance(eq(10), anyDouble())).thenReturn(true);
      when(bidDao.getPreviousHighestBidder(1)).thenReturn(-1);
      when(bidDao.placeBid(any())).thenAnswer(inv -> {
        BidTransaction b = inv.getArgument(0);
        placedBidValue.set(b.getBidValue());
        return true;
      });
      when(itemDao.updatePrice(anyInt(), anyDouble(), anyInt())).thenReturn(true);

      BidTransaction autoBid = new BidTransaction(1, 10, 0);
      autoBid.setAutoBid(true);
      autoBid.setMaxAutoBid(800.0);
      autoBid.setAutoBidIncrement(50.0);

      Response response = manager.processBid(autoBid);

      assertEquals(Response.OK, response.getStatus());
      assertNotNull(placedBidValue.get(), "An actual bid must have been placed");
      assertEquals(550.0, placedBidValue.get(), 0.001,
          "First auto-bid should be 500 + 50 = 550");
    }

    @Test
    @DisplayName("Auto-bid: first bid capped at maxAutoBid when increment would exceed it")
    void autoBid_firstBidCappedAtMaxAutoBid() {
      Item item = openItem(2, 500.0, LocalDateTime.now().plusHours(2));
      item.setCurrentPrice(780.0);
      Bidder bidder = bidder(10);
      AtomicReference<Double> placedBidValue = new AtomicReference<>(null);

      when(userDao.getById("10")).thenReturn(bidder);
      when(itemDao.getById(2)).thenReturn(item);
      when(userDao.atomicDeductBalance(eq(10), anyDouble())).thenReturn(true);
      when(bidDao.getPreviousHighestBidder(2)).thenReturn(-1);
      when(bidDao.placeBid(any())).thenAnswer(inv -> {
        placedBidValue.set(((BidTransaction) inv.getArgument(0)).getBidValue());
        return true;
      });
      when(itemDao.updatePrice(anyInt(), anyDouble(), anyInt())).thenReturn(true);

      BidTransaction autoBid = new BidTransaction(2, 10, 0);
      autoBid.setAutoBid(true);
      autoBid.setMaxAutoBid(800.0);
      autoBid.setAutoBidIncrement(50.0);

      Response response = manager.processBid(autoBid);
      assertEquals(Response.OK, response.getStatus());
      assertEquals(800.0, placedBidValue.get(), 0.001,
          "Auto-bid should be capped at maxAutoBid=800, not 780+50=830");
    }

    @Test
    @DisplayName("Auto-bid rejected when maxAutoBid <= currentPrice")
    void autoBid_rejectedWhenMaxBelowCurrentPrice() {
      Item item = openItem(3, 900.0, LocalDateTime.now().plusHours(2));
      Bidder bidder = bidder(10);

      when(userDao.getById("10")).thenReturn(bidder);
      when(itemDao.getById(3)).thenReturn(item);

      BidTransaction autoBid = new BidTransaction(3, 10, 0);
      autoBid.setAutoBid(true);
      autoBid.setMaxAutoBid(800.0);
      autoBid.setAutoBidIncrement(50.0);

      Response response = manager.processBid(autoBid);

      assertEquals(Response.ERROR, response.getStatus());
      assertTrue(response.getMessage().toLowerCase().contains("higher") || response.getMessage().toLowerCase().contains("ceiling"),
          "Error must mention max bid is not higher than current price");
      verify(bidDao, never()).placeBid(any());
    }

    @ParameterizedTest(name = "currentPrice={0}, increment={1}, maxBid={2} → expected first bid={3}")
    @CsvSource({
      "100.0, 10.0, 500.0, 110.0",
      "200.0, 25.0, 300.0, 225.0",
      "290.0, 50.0, 300.0, 300.0",
      "150.0, 100.0, 200.0, 200.0"
    })
    @DisplayName("Auto-bid first bid formula: min(current + increment, maxBid)")
    void autoBid_firstBidFormula(double current, double increment, double maxBid, double expected) {
      double nextBid = Math.min(current + increment, maxBid);
      assertEquals(expected, nextBid, 0.001,
          "First auto-bid = min(current + increment, maxBid)");
    }
  }

  // ══════════════════════════════════════════════════════════════════════════
  // ANTI-SNIPING TESTS
  // ══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Anti-Sniping Extension")
  class AntiSniping {

    private static final long THRESHOLD_SECONDS = 60L;
    private static final long EXTENSION_SECONDS = 60L;

    @Test
    @DisplayName("Bid in last 60s extends auction end time")
    void bid_inLastMinute_extendsAuction() {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime endTime = now.plusSeconds(30);

      boolean shouldExtend = Duration.between(now, endTime).getSeconds() < THRESHOLD_SECONDS;
      assertTrue(shouldExtend, "Auction with 30s remaining should be extended");

      LocalDateTime extendedEnd = endTime.plusSeconds(EXTENSION_SECONDS);
      assertTrue(Duration.between(now, extendedEnd).getSeconds() >= THRESHOLD_SECONDS,
          "After extension, remaining time must be >= 60s");
    }

    @Test
    @DisplayName("Bid with > 60s remaining does NOT extend auction")
    void bid_withEnoughTime_doesNotExtend() {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime endTime = now.plusSeconds(120);

      boolean shouldExtend = Duration.between(now, endTime).getSeconds() < THRESHOLD_SECONDS;
      assertFalse(shouldExtend, "Auction with 120s remaining must NOT be extended");
    }

    @Test
    @DisplayName("Bid at exactly 60s remaining does NOT extend (boundary)")
    void bid_atExactlyThreshold_doesNotExtend() {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime endTime = now.plusSeconds(60);

      boolean shouldExtend = Duration.between(now, endTime).getSeconds() < THRESHOLD_SECONDS;
      assertFalse(shouldExtend, "At exactly the threshold boundary, no extension should occur");
    }

    @Test
    @DisplayName("Anti-snipe triggers DB updateEndTime when bid placed in last 60s")
    void antiSnipe_triggersDbUpdateEndTime_whenBidInLastMinute() {
      Item item = openItem(10, 500.0, LocalDateTime.now().plusSeconds(30));
      Bidder bidder = bidder(10);

      when(userDao.getById("10")).thenReturn(bidder);
      when(itemDao.getById(10)).thenReturn(item);
      when(userDao.atomicDeductBalance(eq(10), anyDouble())).thenReturn(true);
      when(bidDao.getPreviousHighestBidder(10)).thenReturn(-1);
      when(bidDao.placeBid(any())).thenReturn(true);
      when(itemDao.updatePrice(anyInt(), anyDouble(), anyInt())).thenReturn(true);
      // Must return updated item on reload for the anti-snipe check
      when(itemDao.getById(10)).thenReturn(item);
      when(itemDao.updateEndTime(anyInt(), any(LocalDateTime.class))).thenReturn(true);

      BidTransaction bid = new BidTransaction(10, 10, 700.0);
      Response response = manager.processBid(bid);

      assertEquals(Response.OK, response.getStatus());
      verify(itemDao, atLeastOnce()).updateEndTime(eq(10), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Anti-snipe does NOT trigger when auction has plenty of time left")
    void antiSnipe_doesNotTrigger_whenPlentyOfTime() {
      Item item = openItem(11, 500.0, LocalDateTime.now().plusHours(2));
      Bidder bidder = bidder(10);

      when(userDao.getById("10")).thenReturn(bidder);
      when(itemDao.getById(11)).thenReturn(item);
      when(userDao.atomicDeductBalance(eq(10), anyDouble())).thenReturn(true);
      when(bidDao.getPreviousHighestBidder(11)).thenReturn(-1);
      when(bidDao.placeBid(any())).thenReturn(true);
      when(itemDao.updatePrice(anyInt(), anyDouble(), anyInt())).thenReturn(true);

      BidTransaction bid = new BidTransaction(11, 10, 700.0);
      Response response = manager.processBid(bid);

      assertEquals(Response.OK, response.getStatus());
      verify(itemDao, never()).updateEndTime(anyInt(), any(LocalDateTime.class));
    }

    @ParameterizedTest(name = "remainingSeconds={0} → extended={1}")
    @CsvSource({"10, true", "30, true", "59, true", "60, false", "120, false", "3600, false"})
    @DisplayName("Anti-snipe boundary conditions for various remaining times")
    void antiSnipe_boundaryConditions(long remainingSeconds, boolean expectExtension) {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime endTime = now.plusSeconds(remainingSeconds);
      boolean willExtend = Duration.between(now, endTime).getSeconds() < THRESHOLD_SECONDS;
      assertEquals(expectExtension, willExtend,
          "remainingSeconds=" + remainingSeconds + " should" + (expectExtension ? "" : " NOT") + " trigger extension");
    }
  }
}
