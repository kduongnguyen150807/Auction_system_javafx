package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TransactionLog implements Serializable {
  private static final long serialVersionUID = 1L;
  private int id;
  private int userId;
  private String type;
  private double amount;
  private int itemId;
  private LocalDateTime createdAt;

  public TransactionLog() {}

  public TransactionLog(int u, String t, double a, int i, LocalDateTime c) {
    this.userId = u;
    this.type = t;
    this.amount = a;
    this.itemId = i;
    this.createdAt = c;
  }

  public int getId() {
    int ans = this.id;
    return ans;
  }

  public void setId(int res) {
    this.id = res;
  }

  public int getUserId() {
    int ans = this.userId;
    return ans;
  }

  public void setUserId(int res) {
    this.userId = res;
  }

  public String getType() {
    String ans = this.type;
    return ans;
  }

  public void setType(String res) {
    this.type = res;
  }

  public double getAmount() {
    double ans = this.amount;
    return ans;
  }

  public void setAmount(double res) {
    this.amount = res;
  }

  public int getItemId() {
    int ans = this.itemId;
    return ans;
  }

  public void setItemId(int res) {
    this.itemId = res;
  }

  public LocalDateTime getCreatedAt() {
    LocalDateTime ans = this.createdAt;
    return ans;
  }

  public void setCreatedAt(LocalDateTime res) {
    this.createdAt = res;
  }
}
