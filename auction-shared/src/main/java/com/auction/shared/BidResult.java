package com.auction.shared;

import java.io.Serializable;

public class BidResult extends ResultBase{
  private final double currentPrice;
  private final int winnerId;

  public final static double NON_EXISTING_PRICE = -1;
  public final static int NON_EXISTING_WINNER = -1;

  public final static double EXCEPTION_PRICE = -2;
  public final static int EXCEPTION_WINNER = -2;

  public BidResult(boolean success, String message, double currentPrice, int winnerId) {
    super(success,  message);
    this.currentPrice = currentPrice;
    this.winnerId = winnerId;
  }

  public static BidResult success(String message, double currentPrice, int winnerId) {
    return new BidResult(true, message, currentPrice, winnerId);
  }

  public static BidResult failure(String message, double currentPrice, int winnerId) {
    return new BidResult(false, message, currentPrice, winnerId);
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public int getWinnerId() {
    return winnerId;
  }


}
