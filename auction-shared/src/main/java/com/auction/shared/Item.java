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
  protected String winnerUsername;
  protected String category;

  public Item() {}

  public Item(String name, String description, double startingPrice, double currentPrice, int sellerId) {
    this.name = name;
    this.description = description;
    this.startingPrice = startingPrice;
    this.currentPrice = currentPrice;
    this.sellerId = sellerId;
    this.winnerId = -1;
    this.status = ItemStatus.OPEN;
  }

  public abstract double calculateTax();

  public String getCategory() {
    return this.category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getStartingPrice() {
    return this.startingPrice;
  }

  public void setStartingPrice(double startingPrice) {
    this.startingPrice = startingPrice;
  }

  public double getCurrentPrice() {
    return this.currentPrice;
  }

  public void setCurrentPrice(double currentPrice) {
    this.currentPrice = currentPrice;
  }

  public LocalDateTime getStartTime() {
    return this.startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public LocalDateTime getEndTime() {
    return this.endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public double getMaxPrice() {
    return this.maxPrice;
  }

  public void setMaxPrice(double maxPrice) {
    this.maxPrice = maxPrice;
  }

  public int getSellerId() {
    return this.sellerId;
  }

  public void setSellerId(int sellerId) {
    this.sellerId = sellerId;
  }

  public int getWinnerId() {
    return this.winnerId;
  }

  public void setWinnerId(int winnerId) {
    this.winnerId = winnerId;
  }

  public ItemStatus getStatus() {
    return this.status;
  }

  public void setStatus(ItemStatus status) {
    this.status = status;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getSellerUsername() {
    return this.sellerUsername;
  }

  public void setSellerUsername(String sellerUsername) {
    this.sellerUsername = sellerUsername;
  }

  public String getSellerAvatarUrl() {
    return this.sellerAvatarUrl;
  }

  public void setSellerAvatarUrl(String sellerAvatarUrl) {
    this.sellerAvatarUrl = sellerAvatarUrl;
  }

  public String getWinnerUsername() {
    return this.winnerUsername;
  }

  public void setWinnerUsername(String winnerUsername) {
    this.winnerUsername = winnerUsername;
  }
}
