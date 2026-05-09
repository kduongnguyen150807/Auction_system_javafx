package com.auction.client.util;

import com.auction.shared.Item;

/** Vietnamese UI strings for item-related notifications (bell list). */
public final class ItemNotificationText {

  private ItemNotificationText() {}

  /** {@code Sản phẩm '[Name]' (#[ID])} — uses {@code ?} when name is missing. */
  public static String itemPhrase(Item item) {
    int id = item != null ? item.getId() : -1;
    String raw = item != null ? item.getName() : null;
    String name = (raw == null || raw.isBlank()) ? "?" : raw.trim();
    return "Sản phẩm '" + name + "' (#" + id + ")";
  }

  public static String outbid(Item item) {
    return "Bạn đã bị vượt giá ở " + itemPhrase(item) + "!";
  }

  public static String sellerNewBid(Item item, double newBidPrice) {
    return itemPhrase(item) + " có bid mới: " + String.format("%,.0f", newBidPrice) + " VND";
  }

  public static String itemClosed(Item item) {
    return itemPhrase(item) + " đã được đóng đấu giá!";
  }
}
