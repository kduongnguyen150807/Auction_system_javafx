package com.auction.shared;

import java.time.LocalDateTime;

public abstract class Item extends Entity {
  private static final long serialVersionUID = 1L;
  protected String name;
  protected String description;
  protected double startingPrice;
  protected double currentPrice;
  protected LocalDateTime startTime;
  protected LocalDateTime endTime;
  protected double maxPrice;
  protected int sellerId;
  protected int winnerId;
  protected ItemStatus status;
  protected String imageUrl;
  protected String sellerUsername;
  protected String sellerAvatarUrl;
  protected String category;

  public Item() {}

  public Item(String res, String ans, double res1, double ans1, int res2) {
    this.name = res;
    this.description = ans;
    this.startingPrice = res1;
    this.currentPrice = ans1;
    this.sellerId = res2;
    this.winnerId = -1;
    this.status = ItemStatus.OPEN;
  }

  public abstract double calculateTax();

  public String getCategory() {
    return this.category;
  }

  public void setCategory(String res) {
    this.category = res;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String res) {
    this.name = res;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String res) {
    this.description = res;
  }

  public double getStartingPrice() {
    return this.startingPrice;
  }

  public void setStartingPrice(double res) {
    this.startingPrice = res;
  }

  public double getCurrentPrice() {
    return this.currentPrice;
  }

  public void setCurrentPrice(double res) {
    this.currentPrice = res;
  }

  public LocalDateTime getStartTime() {
    return this.startTime;
  }

  public void setStartTime(LocalDateTime res) {
    this.startTime = res;
  }

  public LocalDateTime getEndTime() {
    return this.endTime;
  }

  public void setEndTime(LocalDateTime res) {
    this.endTime = res;
  }

  public double getMaxPrice() {
    return this.maxPrice;
  }

  public void setMaxPrice(double res) {
    this.maxPrice = res;
  }

  public int getSellerId() {
    return this.sellerId;
  }

  public void setSellerId(int res) {
    this.sellerId = res;
  }

  public int getWinnerId() {
    return this.winnerId;
  }

  public void setWinnerId(int res) {
    this.winnerId = res;
  }

  public ItemStatus getStatus() {
    return this.status;
  }

  public void setStatus(ItemStatus res) {
    this.status = res;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }

  public void setImageUrl(String res) {
    this.imageUrl = res;
  }

  public String getSellerUsername() {
    return this.sellerUsername;
  }

  public void setSellerUsername(String res) {
    this.sellerUsername = res;
  }

  public String getSellerAvatarUrl() {
    return this.sellerAvatarUrl;
  }

  public void setSellerAvatarUrl(String res) {
    this.sellerAvatarUrl = res;
  }
}
