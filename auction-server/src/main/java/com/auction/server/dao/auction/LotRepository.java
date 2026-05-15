package com.auction.server.dao.auction;

import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import java.util.List;

public interface LotRepository {
  List<Item> getOngoingBids(int userId);

  List<Item> getUpcomingBids(int userId);

  List<Item> getClosedBids(int userId);

  List<Item> getPastBids(int userId);

  /** Top-N live OPEN lots ranked by trending score for the auction type (see TrendingLotsFormula). */
  List<Item> getTrendingLiveItems(AuctionType auctionType, int limit);
}
