package com.auction.server.service.auction;

import com.auction.server.live.LiveSessionManager;
import com.auction.shared.AuctionType;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;
import java.util.Set;

/** Live auction bids reuse English rules but require an active live session. */
final class LiveBiddingStrategy implements BiddingStrategy {
  private final LiveSessionManager sessions = LiveSessionManager.getInstance();
  private final BiddingStrategy english;

  LiveBiddingStrategy(BiddingStrategyDependencies deps, BiddingStrategy english) {
    this.english = english;
  }

  @Override
  public Response process(
      BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pending) {
    if (item.getAuctionType() != AuctionType.LIVE) {
      return BidAuctionValidator.error("invalid_auction_type");
    }
    if (!sessions.isParticipant(item.getId(), bid.getUserId())) {
      return BidAuctionValidator.error("not_in_live_session");
    }
    return english.process(bid, item, bidder, after, pending);
  }
}
