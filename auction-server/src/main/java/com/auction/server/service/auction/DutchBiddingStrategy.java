package com.auction.server.service.auction;

import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;
import java.util.Set;

final class DutchBiddingStrategy implements BiddingStrategy {
  private final BiddingStrategyDependencies deps;

  DutchBiddingStrategy(BiddingStrategyDependencies deps) {
    this.deps = deps;
  }

  @Override
  public Response process(
      BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pending) {
    double price = item.getCurrentPrice();
    if (Math.abs(bid.getBidValue() - price) > 0.02) {
      return BidAuctionValidator.error("invalid_dutch_price");
    }

    if (!deps.userDao.atomicDeductBalance(bidder.getId(), price)) {
      return BidAuctionValidator.error("insufficient_balance");
    }

    deps.logDao.insertLog(bidder.getId(), "ITEM_BOUGHT", -price, bid.getItemId());
    deps.userDao.addBidderMetrics(bidder.getId(), price);

    if (!deps.itemDao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED")) {
      deps.userDao.atomicCreditBalance(bidder.getId(), price);
      deps.logDao.insertLog(bidder.getId(), "BUY_REFUND", price, bid.getItemId());
      return BidAuctionValidator.error("auction_already_closed");
    }

    deps.itemDao.updatePrice(item.getId(), price, item.getVersion());
    BiddingSupport.creditSeller(deps, item, price, after);

    after.add(() -> deps.notifier.sendBalanceUpdateToUser(bidder.getId()));
    after.add(() -> deps.notifier.broadcastItemClosed(item.getId()));
    int targetId = item.getId();
    after.add(() -> deps.cleanupAutoBidsForItem.accept(targetId));

    BiddingSupport.updateLeaderboard(deps, bidder, price);
    after.add(() -> AuctionManager.getInstance().broadcastleaderboard());

    return new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
  }
}
