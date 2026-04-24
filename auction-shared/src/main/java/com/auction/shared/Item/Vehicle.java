package com.auction.shared.Item;

public class Vehicle extends Item {
  public Vehicle() {
    super();
  }

  public Vehicle(String res, String ans, double res1, double ans1, int res2) {
    super(res, ans, res1, ans1, res2);
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
