package com.auction.shared;

import java.io.Serializable;

/** Preset quick-bid amounts for a live auction lot. */
public class LiveBidTiers implements Serializable {
  private static final long serialVersionUID = 1L;

  private double currentPrice;
  private double tier1;
  private double tier2;
  private double tier3;

  public LiveBidTiers() {}

  public LiveBidTiers(double currentPrice, double tier1, double tier2, double tier3) {
    this.currentPrice = currentPrice;
    this.tier1 = tier1;
    this.tier2 = tier2;
    this.tier3 = tier3;
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(double currentPrice) {
    this.currentPrice = currentPrice;
  }

  public double getTier1() {
    return tier1;
  }

  public void setTier1(double tier1) {
    this.tier1 = tier1;
  }

  public double getTier2() {
    return tier2;
  }

  public void setTier2(double tier2) {
    this.tier2 = tier2;
  }

  public double getTier3() {
    return tier3;
  }

  public void setTier3(double tier3) {
    this.tier3 = tier3;
  }
}
