package com.auction.server.service.auction;

import com.auction.server.dao.auction.ItemDao;
import com.auction.shared.AuctionType;
import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Aligns listed {@link Item#getCurrentPrice()} with elapsed Dutch drop ticks so catalog + bidding agree.
 */
public final class DutchAuctionCatalogSync {

  private DutchAuctionCatalogSync() {}

  public static void syncMany(ItemDao dao, List<Item> items) {
    if (dao == null || items == null) return;
    for (Item item : items) {
      syncItem(dao, item);
    }
  }

  public static void syncItem(ItemDao dao, Item target) {
    if (dao == null || target == null) return;
    Item db = dao.getById(target.getId());
    if (db == null || db.getAuctionType() != AuctionType.DUTCH) return;
    if (db.getStatus() != ItemStatus.OPEN) return;
    LocalDateTime now = LocalDateTime.now();
    if (db.getEndTime() != null && !db.getEndTime().isAfter(now)) return;

    double computed = DutchAuctionPricing.computeEffectivePrice(db, now);
    if (Math.abs(computed - db.getCurrentPrice()) > 1e-4) {
      dao.updatePrice(db.getId(), computed, db.getVersion());
      db = dao.getById(target.getId());
      if (db == null) return;
    }
    mirrorAuctionRuntimeFields(target, db);
  }

  private static void mirrorAuctionRuntimeFields(Item to, Item from) {
    to.setAuctionType(from.getAuctionType());
    to.setStartingPrice(from.getStartingPrice());
    to.setCurrentPrice(from.getCurrentPrice());
    to.setMaxPrice(from.getMaxPrice());
    to.setVersion(from.getVersion());
    to.setStartTime(from.getStartTime());
    to.setEndTime(from.getEndTime());
    to.setDutchReservePrice(from.getDutchReservePrice());
    to.setDutchTickAmount(from.getDutchTickAmount());
    to.setDutchTickIntervalMinutes(from.getDutchTickIntervalMinutes());
  }
}
