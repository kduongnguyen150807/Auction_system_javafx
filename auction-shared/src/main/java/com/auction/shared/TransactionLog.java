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

  public TransactionLog(int userId, String type, double amount, int itemId, LocalDateTime createdAt) {
    this.userId = userId;
    this.type = type;
    this.amount = amount;
    this.itemId = itemId;
    this.createdAt = createdAt;
  }

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getUserId() {
    return this.userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public double getAmount() {
    return this.amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public int getItemId() {
    return this.itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
