package com.auction.server.service.auction;

import com.auction.shared.AuctionType;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Registry of bidding strategies — open for extension, closed for modification. */
final class BiddingStrategyRegistry {
  private final Map<AuctionType, BiddingStrategy> strategies = new EnumMap<>(AuctionType.class);

  BiddingStrategyRegistry(BiddingStrategyDependencies deps) {
    strategies.put(AuctionType.ENGLISH, new EnglishBiddingStrategy(deps));
    strategies.put(AuctionType.DUTCH, new DutchBiddingStrategy(deps));
    strategies.put(AuctionType.LIVE, new LiveBiddingStrategy(deps, new EnglishBiddingStrategy(deps)));
  }

  BiddingStrategy resolve(AuctionType type) {
    BiddingStrategy strategy = strategies.get(type);
    return strategy != null ? strategy : strategies.get(AuctionType.ENGLISH);
  }
}

final class BiddingSupport {
  private BiddingSupport() {}

  static void creditSeller(
      BiddingStrategyDependencies deps, Item item, double amount, List<Runnable> after) {
    deps.userDao.atomicCreditBalance(item.getSellerId(), amount);
    deps.userDao.addSellerMetrics(item.getSellerId(), amount);
    deps.logDao.insertLog(item.getSellerId(), "ITEM_SOLD", amount, item.getId());
    after.add(() -> deps.notifier.sendBalanceUpdateToUser(item.getSellerId()));
  }

  static void applyAntiSnipeExtension(BiddingStrategyDependencies deps, int itemId) {
    Item item = deps.itemDao.getById(itemId);
    if (item != null && item.getEndTime() != null) {
      long diff = Duration.between(LocalDateTime.now(), item.getEndTime()).getSeconds();
      if (diff < 60) {
        LocalDateTime newEnd = item.getEndTime().plusSeconds(60);
        deps.itemDao.updateEndTime(item.getId(), newEnd);
        SettlementService.getInstance().schedule(item.getId(), newEnd);
      }
    }
  }

  static void updateLeaderboard(BiddingStrategyDependencies deps, User bidder, double amount) {
    AuctionManager.getInstance()
        .getLeaderboardservice()
        .updatescore(bidder.getId(), bidder.getUsername(), bidder.getAvatarUrl(), amount);
  }
}
