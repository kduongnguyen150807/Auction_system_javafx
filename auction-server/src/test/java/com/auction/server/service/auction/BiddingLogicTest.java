package com.auction.server.service.auction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.auction.shared.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Bidding Logic — Rules & Exceptions")
public class BiddingLogicTest extends AbstractAuctionManagerMockingTest {

  private AuctionManager manager;

  @BeforeEach
  void setUp() {
    manager = new AuctionManager(itemDao, userDao, bidDao, logDao);
    bindAuctionManagerSingleton(manager);
  }

  private Item openItem(int id, double currentPrice) {
    Item item = ItemFactory.createItem("Electronics");
    item.setId(id);
    item.setSellerId(99);
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
  @DisplayName("Valid bid higher than current price is accepted")
  void validBid_higherThanCurrentPrice_accepted() throws SQLException {
    Item item = openItem(1, 500.0);
    Bidder bidder = verifiedBidder(10, 10_000.0);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(1)).thenReturn(item);
    stubSuccessfulEnglishBid(1, 10, 700.0, -1);

    BidTransaction bid = new BidTransaction(1, 10, 700.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.OK, response.getStatus());
    verify(bidDao).placeBidTx(any(), eq(jdbcConn));
  }

  @Test
  @DisplayName("Bid equal to current price is rejected")
  void bid_equalToCurrentPrice_rejected() throws SQLException {
    Item item = openItem(2, 500.0);
    Bidder bidder = verifiedBidder(10, 10_000.0);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(2)).thenReturn(item);

    BidTransaction bid = new BidTransaction(2, 10, 500.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
    assertTrue(response.getMessage().toLowerCase().contains("low") || response.getMessage().toLowerCase().contains("bid"),
        "Error must mention bid too low");
    verify(bidDao, never()).placeBidTx(any(), any());
  }

  @ParameterizedTest(name = "bid={0} vs currentPrice=500 → rejected")
  @ValueSource(doubles = {499.99, 400.0, 100.0, 0.01})
  @DisplayName("Any bid below current price is rejected")
  void bid_belowCurrentPrice_rejected(double bidAmount) throws SQLException {
    Item item = openItem(3, 500.0);
    Bidder bidder = verifiedBidder(10, 10_000.0);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(3)).thenReturn(item);

    BidTransaction bid = new BidTransaction(3, 10, bidAmount);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
    verify(bidDao, never()).placeBidTx(any(), any());
  }

  @Test
  @DisplayName("Bid on CLOSED auction is rejected")
  void bid_onClosedAuction_rejected() {
    Item item = openItem(4, 500.0);
    item.setStatus(ItemStatus.CLOSED);
    Bidder bidder = verifiedBidder(10, 10_000.0);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(4)).thenReturn(item);

    BidTransaction bid = new BidTransaction(4, 10, 700.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
  }

  @Test
  @DisplayName("Locked user bid is rejected before acquiring auction lock")
  void lockedUser_bidRejectedEarly() {
    Bidder bidder = verifiedBidder(10, 10_000.0);
    bidder.setLocked(true);

    when(userDao.getById("10")).thenReturn(bidder);

    BidTransaction bid = new BidTransaction(1, 10, 700.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
    assertTrue(response.getMessage().toLowerCase().contains("suspended") || response.getMessage().toLowerCase().contains("lock"),
        "Error must mention suspended/locked account");
    verify(itemDao, never()).getById(anyInt());
  }

  @Test
  @DisplayName("Bid rejected when bidder has insufficient balance")
  void insufficientBalance_bidRejected() throws SQLException {
    Item item = openItem(6, 500.0);
    Bidder bidder = verifiedBidder(10, 50.0);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(6)).thenReturn(item);
    when(userDao.deductBalanceTx(eq(10), eq(700.0), eq(jdbcConn))).thenReturn(false);

    BidTransaction bid = new BidTransaction(6, 10, 700.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
    assertTrue(response.getMessage().toLowerCase().contains("balance") || response.getMessage().toLowerCase().contains("insufficient"));
    verify(bidDao, never()).placeBidTx(any(), any());
  }

  @Test
  @DisplayName("Unverified user (no phone) cannot bid")
  void unverifiedUser_nophoneNumber_rejected() {
    Item item = openItem(7, 500.0);
    Bidder bidder = new Bidder("noPhone", "pass", "e@test.com", "25", null);
    bidder.setId(10);
    bidder.setActive(true);
    bidder.setLocked(false);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(7)).thenReturn(item);

    BidTransaction bid = new BidTransaction(7, 10, 700.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.ERROR, response.getStatus());
    assertTrue(response.getMessage().toLowerCase().contains("phone") || response.getMessage().toLowerCase().contains("verified"));
  }

  @Test
  @DisplayName("Bid equal to maxPrice triggers Buy-It-Now success")
  void bidAtMaxPrice_triggersBuyItNow() {
    Item item = openItem(8, 500.0);
    item.setMaxPrice(1000.0);
    Bidder bidder = verifiedBidder(10, 50_000.0);

    when(userDao.getById("10")).thenReturn(bidder);
    when(itemDao.getById(8)).thenReturn(item);
    when(userDao.atomicDeductBalance(10, 1000.0)).thenReturn(true);
    when(itemDao.atomicCloseAuction(8, 10, "CLOSED")).thenReturn(true);
    when(itemDao.updatePrice(anyInt(), anyDouble(), anyInt())).thenReturn(true);
    when(userDao.atomicCreditBalance(anyInt(), anyDouble())).thenReturn(true);

    BidTransaction bid = new BidTransaction(8, 10, 1000.0);
    Response response = manager.processBid(bid);

    assertEquals(Response.OK, response.getStatus());
    assertEquals("BUY_IT_NOW_SUCCESS", response.getMessage());
  }
}
