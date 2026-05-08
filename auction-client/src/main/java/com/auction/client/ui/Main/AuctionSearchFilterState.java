package com.auction.client.ui.Main;

import com.auction.shared.AuctionType;

/** Holds auction home / my-items filter criteria for the main shell (no UI). */
final class AuctionSearchFilterState {

  private String keyword = "";
  private String category = "All";
  private double minPrice = 0;
  private double maxPrice = Double.MAX_VALUE;
  /** Home catalog tab: English vs Dutch lots (default English). */
  private AuctionType catalogAuctionType = AuctionType.ENGLISH;

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

  AuctionType getCatalogAuctionType() {
    return catalogAuctionType != null ? catalogAuctionType : AuctionType.ENGLISH;
  }

  void setCatalogAuctionType(AuctionType auctionType) {
    this.catalogAuctionType = auctionType != null ? auctionType : AuctionType.ENGLISH;
  }

  void apply(String keyword, String category, double min, double max) {
    this.keyword = keyword == null ? "" : keyword.trim();
    this.category = category == null ? "All" : category.trim();
    this.minPrice = min;
    this.maxPrice = max;
  }
}

