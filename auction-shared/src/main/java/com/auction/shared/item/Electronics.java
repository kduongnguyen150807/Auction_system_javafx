package com.auction.shared.item;

public class Electronics extends Item {
  public Electronics() {
    super();
    this.category = ItemType.ELECTRONICS;
  }

  @Override
  public double calculateTax() {
    return this.currentPrice * 0.05;
  }
}
