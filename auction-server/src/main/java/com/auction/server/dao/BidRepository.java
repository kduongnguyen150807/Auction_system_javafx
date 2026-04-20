package com.auction.server.dao;

import com.auction.shared.BidTransaction;
import java.util.List;

public interface BidRepository {
  boolean placeBid(BidTransaction bid);
  boolean addBid(BidTransaction bid);
  List<BidTransaction> getByItem(int itemId);
  BidTransaction getWinner(int itemId);
  List<BidTransaction> getBidHistory(int itemId);
  int getPreviousHighestBidder(int itemId);
}
