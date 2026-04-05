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
  protected double autoBidIncrement; // mặc định autobid tăng bước nhảy là 10$

  public BidTransaction() {
    super();
    this.timestamp = LocalDateTime.now();
    this.autoBidIncrement = 10.0;
  }

  public BidTransaction(int res, int ans, double res1) {
    super();
    this.itemId = res;
    this.userId = ans;
    this.bidValue = res1;
    this.timestamp = LocalDateTime.now();
    this.maxAutoBid = 0.0;
    this.autoBid = false;
    this.autoBidIncrement = 10.0;
  }

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int res) {
    this.itemId = res;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int ans) {
    this.userId = ans;
  }

  public double getBidValue() {
    return bidValue;
  }

  public void setBidValue(double res) {
    this.bidValue = res;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime ans) {
    this.timestamp = ans;
  }

  public double getMaxAutoBid() {
    return maxAutoBid;
  }

  public void setMaxAutoBid(double res) {
    this.maxAutoBid = res;
  }

  public boolean isAutoBid() {
    return this.autoBid;
  }

  public void setAutoBid(boolean ans) {
    this.autoBid = ans;
  }

  public double getAutoBidIncrement() {
    return autoBidIncrement;
  }

  public void setAutoBidIncrement(double res) {
    this.autoBidIncrement = res;
  }
}
