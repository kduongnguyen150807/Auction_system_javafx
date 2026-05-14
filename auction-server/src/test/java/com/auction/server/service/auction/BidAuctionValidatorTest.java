package com.auction.server.service.auction;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.shared.BidTransaction;
import com.auction.shared.Bidder;
import com.auction.shared.Electronics;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Response;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Covers bid eligibility rules from {@link BidAuctionValidator} (open status, start/end window,
 * account and phone verification, self-bid guard).
 */
@DisplayName("BidAuctionValidator")
class BidAuctionValidatorTest {

  private BidAuctionValidator validator;
  private BidTransaction bid;
  private Item item;
  private Bidder bidder;

  @BeforeEach
  void setUp() {
    validator = new BidAuctionValidator();
    bid = new BidTransaction(1, 10, 100.0);
    item = new Electronics("phone", "desc", 50, 50, 5);
    item.setId(1);
    item.setStatus(ItemStatus.OPEN);
    item.setEndTime(LocalDateTime.now().plusDays(1));
    bidder = new Bidder("bob", "secret", "b@x.com", "30", "0901234567");
    bidder.setId(10);
    bidder.setActive(true);
    bidder.setLocked(false);
  }

  private void assertError(Response r, String contains) {
    assertNotNull(r);
    assertEquals(Response.ERROR, r.getStatus());
    assertNotNull(r.getMessage());
    assertTrue(r.getMessage().contains(contains), "expected message containing: " + contains);
  }

  @Nested
  @DisplayName("rejections")
  class Rejections {

    @Test
    void itemNull() {
      assertError(validator.validate(bid, null, bidder), "Item");
    }

    @Test
    void itemNotOpen() {
      item.setStatus(ItemStatus.CLOSED);
      assertError(validator.validate(bid, item, bidder), "open");
    }

    @Test
    void auctionEnded() {
      item.setEndTime(LocalDateTime.now().minusMinutes(1));
      assertError(validator.validate(bid, item, bidder), "ended");
    }

    @Test
    void auctionNotStartedYet() {
      item.setStartTime(LocalDateTime.now().plusHours(2));
      assertError(validator.validate(bid, item, bidder), "auction_not_started");
    }

    @Test
    void bidderNull() {
      assertError(validator.validate(bid, item, null), "User");
    }

    @Test
    void accountLocked() {
      bidder.setLocked(true);
      assertError(validator.validate(bid, item, bidder), "locked");
    }

    @Test
    void accountInactive() {
      bidder.setActive(false);
      assertError(validator.validate(bid, item, bidder), "locked");
    }

    @Test
    void missingPhone() {
      bidder.setPhoneNumber("  ");
      assertError(validator.validate(bid, item, bidder), "phone");
    }

    @Test
    void sellerCannotBidOnOwnLot() {
      item.setSellerId(10);
      assertError(validator.validate(bid, item, bidder), "own item");
    }
  }

  @Nested
  @DisplayName("acceptance")
  class Acceptance {

    @Test
    void happyPath_returnsNull() {
      item.setStartTime(LocalDateTime.now().minusHours(1));
      assertNull(validator.validate(bid, item, bidder));
    }

    @Test
    void startTimeNull_stillAcceptedWhenOpen() {
      item.setStartTime(null);
      assertNull(validator.validate(bid, item, bidder));
    }

    @Test
    void noEndTime_stillAcceptedWhenOpen() {
      item.setEndTime(null);
      assertNull(validator.validate(bid, item, bidder));
    }

    @Test
    void errorStaticFactory_matchesFailedResponseShape() {
      Response err = BidAuctionValidator.error("boom");
      assertEquals(Response.ERROR, err.getStatus());
      assertEquals("boom", err.getMessage());
    }
  }
}
