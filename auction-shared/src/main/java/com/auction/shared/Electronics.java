package com.auction.shared;

public class Electronics extends Item {
  public Electronics() {
    super();
  }

  public Electronics(String name, String description, double startingPrice, double currentPrice, int sellerId) {
    super(name, description, startingPrice, currentPrice, sellerId);
  }

  @Override
  public String getCategory() {
    return "Electronics";
  }

  @Override
  public double calculateTax() {
    return this.currentPrice * 0.05;
  }
}
