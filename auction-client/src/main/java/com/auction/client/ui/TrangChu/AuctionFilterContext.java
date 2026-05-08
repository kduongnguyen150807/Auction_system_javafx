package com.auction.client.ui.TrangChu;

import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Item;
import java.util.ArrayList;
import java.util.List;

/** Keyword / category / price filter state for catalog lists (trending + category lanes). */
public record AuctionFilterContext(
    String keywordLower, String categoryFilter, double minPrice, double maxPrice) {

  public static AuctionFilterContext fromHomeState(String keywordLower, String category) {
    double minPrice = KhungController.getMinPrice();
    double maxPrice = KhungController.getMaxPrice();
    if (maxPrice <= 0) {
      maxPrice = Double.MAX_VALUE;
    }
    String kw = keywordLower == null ? "" : keywordLower;
    String cat =
        (category == null || category.isBlank()) ? "All" : category;
    return new AuctionFilterContext(kw, cat, minPrice, maxPrice);
  }

  public List<Item> itemsMatchingCategoryLane(List<Item> cachedItems, String laneCategory) {
    List<Item> out = new ArrayList<>();
    for (Item item : cachedItems) {
      if (matchesCategoryLane(item, laneCategory)) {
        out.add(item);
      }
    }
    return out;
  }

  public List<Item> itemsMatchingTrending(List<Item> cachedItems) {
    List<Item> out = new ArrayList<>();
    for (Item item : cachedItems) {
      if (matchesTrending(item)) {
        out.add(item);
      }
    }
    return out;
  }

  public boolean matchesTrending(Item item) {
    if (!matchesKeywordAndPrice(item)) {
      return false;
    }
    if (categoryFilter != null
        && !categoryFilter.equalsIgnoreCase("All")
        && item.getCategory() != null
        && !item.getCategory().equalsIgnoreCase(categoryFilter)) {
      return false;
    }
    return true;
  }

  private boolean laneVisibleGlobally(String laneCategory) {
    return categoryFilter.equalsIgnoreCase("All")
        || categoryFilter.equalsIgnoreCase(laneCategory);
  }

  public boolean matchesCategoryLane(Item item, String laneCategory) {
    if (!laneVisibleGlobally(laneCategory)) {
      return false;
    }
    if (!matchesKeywordAndPrice(item)) {
      return false;
    }
    return laneCategory.equalsIgnoreCase(nullToEmpty(item.getCategory()));
  }

  private boolean matchesKeywordAndPrice(Item item) {
    String name = nullToEmpty(item.getName()).toLowerCase();
    if (!keywordLower.isBlank() && !name.contains(keywordLower)) {
      return false;
    }
    return item.getCurrentPrice() >= minPrice && item.getCurrentPrice() <= maxPrice;
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
