package com.auction.server.dao.auction;

import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public interface ItemRepository {
  List<Item> getAll();
  Item getById(int id);
  List<Item> getBySellerId(int sellerId);
  List<Item> getPendingItems();
  boolean updatePrice(int itemId, double price, int version);
  boolean updateEndTime(int itemId, LocalDateTime endTime);
  boolean insertLot(
      String name,
      String description,
      double startingPrice,
      double maxPrice,
      LocalDateTime startTime,
      LocalDateTime endTime,
      String sellerUsername,
      String imageUrl,
      String category,
      AuctionType auctionType,
      double dutchReservePrice,
      double dutchTickAmount,
      int dutchTickIntervalMinutes);
  void closeAuction(int itemId, int winnerId, String status);
  boolean approveItem(int itemId);
  boolean rejectItem(int itemId);
  HashMap<String, Integer> getStatusStats();
  HashMap<String, Double> getCategoryStats();
}
