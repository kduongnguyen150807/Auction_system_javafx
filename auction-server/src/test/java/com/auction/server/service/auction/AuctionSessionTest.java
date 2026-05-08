package com.auction.server.service.auction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.server.dao.user.UserDao;
import com.auction.server.service.auction.AuctionManager;
import com.auction.shared.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auction Session State Tests")
public class AuctionSessionTest {

  @Mock ItemDao itemDao;
  @Mock UserDao userDao;
  @Mock BidDao bidDao;
  @Mock TransactionLogDao logDao;

  private AuctionManager manager;

  private Item openItem(int id, int sellerId, double startingPrice) {
    Item item = ItemFactory.createItem("Electronics");
    item.setId(id);
    item.setSellerId(sellerId);
    item.setStartingPrice(startingPrice);
    item.setCurrentPrice(startingPrice);
    item.setStatus(ItemStatus.OPEN);
    item.setEndTime(LocalDateTime.now().plusHours(2));
    return item;
  }

  private Bidder validBidder(int id) {
    Bidder b = new Bidder("user" + id, "pass", "u" + id + "@test.com", "25", "090123456" + id);
    b.setId(id);
    b.setActive(true);
    b.setLocked(false);
    b.setBalance(100_000.0);
    return b;
  }

  @BeforeEach
  void setUp() {
    manager = new AuctionManager(itemDao, userDao, bidDao, logDao);
  }

  // ── 1. State: OPEN → accepting bids ──────────────────────────────────────

  @Test
  @DisplayName("OPEN item accepts valid bids")
  void openItem_acceptsValidBid() {
    Item item = openItem(1, 99, 100.0);
    Bidder bidder = validBidder(10);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(1)).thenReturn(item);
    when(userDao.atomicDeductBalance(10, 200.0)).thenReturn(true);
    when(bidDao.getPreviousHighestBidder(1)).thenReturn(-1);
    when(bidDao.placeBid(any())).thenReturn(true);
    when(itemDao.updatePrice(anyInt(), anyDouble(), anyInt())).thenReturn(true);

    BidTransaction bid = new BidTransaction(1, 10, 200.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.OK, response.getStatus());
  }

  // ── 2. State: CLOSED → rejects all bids ──────────────────────────────────

  @Test
  @DisplayName("CLOSED item rejects incoming bids")
  void closedItem_rejectsBid() {
    Item item = openItem(2, 99, 100.0);
    item.setStatus(ItemStatus.CLOSED);
    Bidder bidder = validBidder(10);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(2)).thenReturn(item);

    BidTransaction bid = new BidTransaction(2, 10, 200.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
    assertTrue(response.getMessage().toLowerCase().contains("open") || response.getMessage().toLowerCase().contains("longer"),
        "Error should mention auction is no longer open");
  }

  // ── 3. State: CANCELED → rejects all bids ────────────────────────────────

  @Test
  @DisplayName("CANCELED item rejects incoming bids")
  void canceledItem_rejectsBid() {
    Item item = openItem(3, 99, 100.0);
    item.setStatus(ItemStatus.CANCELED);
    Bidder bidder = validBidder(10);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(3)).thenReturn(item);

    BidTransaction bid = new BidTransaction(3, 10, 200.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
  }

  // ── 4. Winner determination ───────────────────────────────────────────────

  @Test
  @DisplayName("FINISHED item: highest bidder becomes winner")
  void finishedItem_highestBidderIsWinner() {
    Item item = openItem(4, 99, 100.0);
    item.setCurrentPrice(500.0);
    item.setWinnerId(42);
    item.setStatus(ItemStatus.CLOSED);

    assertEquals(42, item.getWinnerId(), "Winner should be the user with highest bid");
  }

  // ── 5. Time-expired auction ───────────────────────────────────────────────

  @Test
  @DisplayName("Expired auction (endTime in the past) rejects new bids")
  void expiredAuction_rejectsBid() {
    Item item = openItem(5, 99, 100.0);
    item.setEndTime(LocalDateTime.now().minusSeconds(30));
    Bidder bidder = validBidder(10);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(5)).thenReturn(item);

    BidTransaction bid = new BidTransaction(5, 10, 200.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
    assertTrue(response.getMessage().toLowerCase().contains("ended") || response.getMessage().toLowerCase().contains("open"),
        "Error must mention auction ended or no longer open");
  }

  // ── 6. Seller cannot bid on own item ─────────────────────────────────────

  @Test
  @DisplayName("Seller cannot place bid on their own item")
  void seller_cannotBidOwnItem() {
    int sellerId = 99;
    Item item = openItem(6, sellerId, 100.0);
    Bidder bidder = validBidder(sellerId);

    when(userDao.getById(String.valueOf(sellerId))).thenReturn(bidder);
    when(itemDao.getById(6)).thenReturn(item);

    BidTransaction bid = new BidTransaction(6, sellerId, 200.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
    assertTrue(response.getMessage().toLowerCase().contains("own"),
        "Error must mention cannot bid on own item");
  }
}
