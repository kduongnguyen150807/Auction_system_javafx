package com.auction.server.domain;

import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionDomain {
  private static final Logger LOG = LoggerFactory.getLogger(AuctionDomain.class);
  public void executeNormalBid(Item item, int bidderId, double bidAmount, double bidderBalance) throws BidException {
    validatebid(item, bidderId, bidAmount, bidderBalance);
    item.setCurrentPrice(bidAmount);
    item.setWinnerId(bidderId);
    LOG.info("AuctionDomain.executeNormalBid {} {} {}", item.getId(), bidderId, bidAmount);
  }

  public void executeBuyNowBid(Item item, int bidderId, double bidAmount, double bidderBalance) throws BidException {
    validatebid(item, bidderId, bidAmount, bidderBalance);
    item.setCurrentPrice(item.getMaxPrice());
    item.setWinnerId(bidderId);
    LOG.info("AuctionDomain.executeBuyNowBid {} {} {}", item.getId(), bidderId, bidAmount);
  }

  private void validatebid(Item item, int bidderId, double bidAmount, double bidderBalance) throws BidException {
    if (item.getStatus() != ItemStatus.OPEN) {
      throw new BidException("ITEM IS NOT OPEN");
    }
    if (item.getSellerId() == bidderId) {
      throw new BidException("SELLER CAN NOT BID OWN ITEM");
    }
    double currentPrice = item.getCurrentPrice();
    if (currentPrice >= bidAmount) {
      throw new BidException("BID AMOUNT IS TOO LOW");
    }
    if (bidderBalance < bidAmount) {
      throw new BidException("BALANCE NOT ENOUGH");
    }
  }
}
