package com.auction.shared.dto;

import java.io.Serializable;

public class BidForm implements Serializable {
  private final int itemId;
  private final int bidderId;
  private final double bidAmount;
  public BidForm(int itemId, int bidderId, double bidAmount) {
    this.itemId = itemId;
    this.bidderId = bidderId;
    this.bidAmount = bidAmount;
  }

  public int getItemId() {
    return itemId;
  }

  public int getBidderId() {
    return bidderId;
  }

  public double getBidAmount() {
    return bidAmount;
  }
}
