package com.auction.shared;

public class Art extends Item {
  public Art() {
    super();
  }

  public Art(String name, String description, double startingPrice, double currentPrice, int sellerId) {
    super(name, description, startingPrice, currentPrice, sellerId);
  }

  @Override
  public String getCategory() {
    return "Art";
  }

  @Override
  public double calculateTax() {
    return this.currentPrice * 0.08;
  }
}
