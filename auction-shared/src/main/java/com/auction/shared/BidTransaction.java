package com.auction.shared;

import java.time.LocalDateTime;

public class BidTransaction extends Entity {
  private static final long serialVersionUID = 1L;
  protected int itemId;
  protected int userId;
  protected double bidValue;
  protected LocalDateTime timestamp;
  protected double maxAutoBid;
  protected boolean autoBid;
  protected double autoBidIncrement;

  public BidTransaction() {
    super();
    this.timestamp = LocalDateTime.now();
    this.autoBidIncrement = 10.0;
  }

  public BidTransaction(int itemId, int userId, double bidValue) {
    super();
    this.itemId = itemId;
    this.userId = userId;
    this.bidValue = bidValue;
    this.timestamp = LocalDateTime.now();
    this.maxAutoBid = 0.0;
    this.autoBid = false;
    this.autoBidIncrement = 10.0;
  }

  public int getItemId() {
    return this.itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public int getUserId() {
    return this.userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public double getBidValue() {
    return this.bidValue;
  }

  public void setBidValue(double bidValue) {
    this.bidValue = bidValue;
  }

  public LocalDateTime getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public double getMaxAutoBid() {
    return this.maxAutoBid;
  }

  public void setMaxAutoBid(double maxAutoBid) {
    this.maxAutoBid = maxAutoBid;
  }

  public boolean isAutoBid() {
    return this.autoBid;
  }

  public void setAutoBid(boolean autoBid) {
    this.autoBid = autoBid;
  }

  public double getAutoBidIncrement() {
    return this.autoBidIncrement;
  }

  public void setAutoBidIncrement(double autoBidIncrement) {
    this.autoBidIncrement = autoBidIncrement;
  }
}
