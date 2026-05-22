package com.auction.server.service.auction;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.user.UserDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.shared.AuctionType;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * Xử lý luồng đặt giá (Bidding Pipeline).
 * Đã sửa lỗi tương thích với Optimistic Locking.
 */
final class AuctionBidPipeline {
  private final ItemDao itemdao;
  private final BidAuctionValidator validator;
  private final BiddingStrategyRegistry strategyRegistry;

  AuctionBidPipeline(
      ItemDao itemdao,
      UserDao userdao,
      BidDao biddao,
      TransactionLogDao logdao,
      AuctionRealtimeNotifier notifier,
      BidAuctionValidator validator) {
    this.itemdao = itemdao;
    this.validator = validator;
    BiddingStrategyDependencies deps =
        new BiddingStrategyDependencies(itemdao, userdao, biddao, logdao, notifier);
    this.strategyRegistry = new BiddingStrategyRegistry(deps);
    this.pipelineDeps = deps;
  }

  private final BiddingStrategyDependencies pipelineDeps;

  void setCleanupAutoBidsForItem(IntConsumer cleanupautobidsforitem) {
    if (cleanupautobidsforitem != null) {
      pipelineDeps.cleanupAutoBidsForItem = cleanupautobidsforitem;
    }
  }

  Response processManualBid(
      BidTransaction bid, List<Runnable> after, Set<Integer> pendingpricebroadcast) {
    Item item = itemdao.getById(bid.getItemId());
    if (item == null) return BidAuctionValidator.error("Item not found");

    DutchAuctionCatalogSync.syncItem(itemdao, item);
    item = itemdao.getById(bid.getItemId());

    User bidder = pipelineDeps.userDao.getById(String.valueOf(bid.getUserId()));
    Response valres = validator.validate(bid, item, bidder);
    if (valres != null) return valres;

    BiddingStrategy strategy = strategyRegistry.resolve(item.getAuctionType());
    return strategy.process(bid, item, bidder, after, pendingpricebroadcast);
  }
}
