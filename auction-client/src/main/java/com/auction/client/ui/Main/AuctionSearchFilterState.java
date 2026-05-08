package com.auction.client.ui.Main;

/** Holds auction home / my-items filter criteria for the main shell (no UI). */
final class AuctionSearchFilterState {

  private String keyword = "";
  private String category = "All";
  private double minPrice = 0;
  private double maxPrice = Double.MAX_VALUE;

  String getKeyword() {
    return keyword;
  }

  String getCategory() {
    return category;
  }

  double getMinPrice() {
    return minPrice;
  }

  double getMaxPrice() {
    return maxPrice;
  }

  void apply(String keyword, String category, double min, double max) {
    this.keyword = keyword == null ? "" : keyword.trim();
    this.category = category == null ? "All" : category.trim();
    this.minPrice = min;
    this.maxPrice = max;
  }
}
