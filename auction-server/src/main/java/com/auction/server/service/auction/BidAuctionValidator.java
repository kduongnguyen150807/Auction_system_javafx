package com.auction.server.service.auction;

import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.time.LocalDateTime;

/** Rules for whether a user may bid on an open item (excluding balance / concurrency). */
final class BidAuctionValidator {

  Response validate(BidTransaction bid, Item item, User bidder) {
    if (item == null) return error("Item not found");
    if (item.getStatus() != ItemStatus.OPEN) return error("Auction is no longer open");
    LocalDateTime now = LocalDateTime.now();
    if (item.getStartTime() != null && item.getStartTime().isAfter(now)) {
      return error("auction_not_started");
    }
    if (item.getEndTime() != null && item.getEndTime().isBefore(now))
      return error("Auction has ended");
    if (bidder == null) return error("User not found");
    if (bidder.isLocked() || !bidder.isActive()) return error("Account is locked");
    String phone = bidder.getPhoneNumber();
    if (phone == null || phone.trim().isEmpty()) return error("Unverified account. Add a phone number to bid.");
    if (item.getSellerId() == bid.getUserId()) return error("Cannot bid on your own item");
    return null;
  }

  static Response error(String msg) {
    return new Response("", Response.ERROR, msg, null);
  }
}
