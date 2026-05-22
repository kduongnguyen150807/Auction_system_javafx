package com.auction.server.service.auction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.auction.server.live.LiveSessionManager;
import com.auction.shared.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Live auction bidding")
class LiveBiddingLogicTest extends AbstractAuctionManagerMockingTest {

  private AuctionManager manager;

  @BeforeEach
  void setUp() {
    LiveSessionManager.resetForTest();
    manager = new AuctionManager(itemDao, userDao, bidDao, logDao);
    bindAuctionManagerSingleton(manager);
  }

  @AfterEach
  void tearDown() {
    LiveSessionManager.resetForTest();
  }

  private Item liveItem(int id, double currentPrice) {
    Item item = ItemFactory.createItem("Electronics");
    item.setId(id);
    item.setSellerId(99);
    item.setAuctionType(AuctionType.LIVE);
    item.setStartingPrice(100.0);
    item.setCurrentPrice(currentPrice);
    item.setStatus(ItemStatus.OPEN);
    item.setEndTime(LocalDateTime.now().plusHours(1));
    return item;
  }

  private Bidder verifiedBidder(int id, double balance) {
    Bidder b = new Bidder("user" + id, "pass", "u@test.com", "25", "09012345" + id);
    b.setId(id);
    b.setActive(true);
    b.setLocked(false);
    b.setBalance(balance);
    return b;
  }

  @Test
  @DisplayName("Bid without joining live session is rejected")
  void bidWithoutSession_rejected() throws SQLException {
    Item item = liveItem(1, 500.0);
    Bidder bidder = verifiedBidder(10, 10_000.0);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(1)).thenReturn(item);

    BidTransaction bid = new BidTransaction(1, 10, 700.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
    assertEquals("not_in_live_session", response.getMessage());
    verify(bidDao, never()).placeBidTx(any(), any());
  }

  @Test
  @DisplayName("Bid after joining live session is accepted")
  void bidAfterJoin_accepted() throws SQLException {
    Item item = liveItem(2, 500.0);
    Bidder bidder = verifiedBidder(10, 10_000.0);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(2)).thenReturn(item);
    stubSuccessfulEnglishBid(2, 10, 700.0, -1);

    LiveSessionManager.getInstance().join(bidder, 2, 9090);

    BidTransaction bid = new BidTransaction(2, 10, 700.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.OK, response.getStatus());
    verify(bidDao).placeBidTx(any(), eq(jdbcConn));
  }
}
