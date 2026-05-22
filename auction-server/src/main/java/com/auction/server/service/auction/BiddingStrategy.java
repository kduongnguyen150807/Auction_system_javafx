package com.auction.server.service.auction;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.user.UserDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

/** Shared dependencies for pluggable bidding strategies (OCP). */
interface BiddingStrategy {
  Response process(
      BidTransaction bid,
      Item item,
      User bidder,
      List<Runnable> after,
      Set<Integer> pendingPriceBroadcast);
}

final class BiddingStrategyDependencies {
  final ItemDao itemDao;
  final UserDao userDao;
  final BidDao bidDao;
  final TransactionLogDao logDao;
  final AuctionRealtimeNotifier notifier;
  IntConsumer cleanupAutoBidsForItem = id -> {};

  BiddingStrategyDependencies(
      ItemDao itemDao,
      UserDao userDao,
      BidDao bidDao,
      TransactionLogDao logDao,
      AuctionRealtimeNotifier notifier) {
    this.itemDao = itemDao;
    this.userDao = userDao;
    this.bidDao = bidDao;
    this.logDao = logDao;
    this.notifier = notifier;
  }
}
