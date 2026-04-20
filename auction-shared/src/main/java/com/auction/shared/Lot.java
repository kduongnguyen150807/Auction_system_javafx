package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Lot implements Serializable {
  private static final long serialVersionUID = 1L;
  private int id;
  private int itemId;
  private String title;
  private String description;
  private double bidValue;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String imageUrl;
  private String sellerUsername;
  private String sellerAvatarUrl;
  private String winnerUsername;

  public Lot() {}

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getItemId() {
    return this.itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getBidValue() {
    return this.bidValue;
  }

  public void setBidValue(double bidValue) {
    this.bidValue = bidValue;
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
