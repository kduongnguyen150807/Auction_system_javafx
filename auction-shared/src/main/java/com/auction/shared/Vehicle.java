package com.auction.shared;

public class Vehicle extends Item {
  public Vehicle() {
    super();
  }

  public Vehicle(String name, String description, double startingPrice, double currentPrice, int sellerId) {
    super(name, description, startingPrice, currentPrice, sellerId);
  }

  @Override
  public String getCategory() {
    return "Vehicle";
  }

  @Override
  public double calculateTax() {
    return this.currentPrice * 0.10;
  }
}
