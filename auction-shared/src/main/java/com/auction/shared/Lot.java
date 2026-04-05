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
    return id;
  }

  public void setId(int i) {
    this.id = i;
  }

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int i) {
    this.itemId = i;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String t) {
    this.title = t;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String d) {
    this.description = d;
  }

  public double getBidValue() {
    return bidValue;
  }

  public void setBidValue(double v) {
    this.bidValue = v;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime t) {
    this.startTime = t;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime t) {
    this.endTime = t;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String u) {
    this.imageUrl = u;
  }

  public String getSellerUsername() {
    return this.sellerUsername;
  }

  public void setSellerUsername(String s) {
    this.sellerUsername = s;
  }

  public String getSellerAvatarUrl() {
    return this.sellerAvatarUrl;
  }

  public void setSellerAvatarUrl(String s) {
    this.sellerAvatarUrl = s;
  }

  public String getWinnerUsername() {
    return this.winnerUsername;
  }

  public void setWinnerUsername(String w) {
    this.winnerUsername = w;
  }
}
