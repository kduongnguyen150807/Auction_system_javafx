package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Rating implements Serializable {
  private static final long serialVersionUID = 1L;
  private int id;
  private int itemId;
  private int raterUserId;
  private int ratedUserId;
  private int stars;
  private String feedback;
  private LocalDateTime createdAt;
  private String raterUsername;

  public Rating() {}

  public Rating(int itemId, int raterUserId, int ratedUserId, int stars, String feedback) {
    this.itemId = itemId;
    this.raterUserId = raterUserId;
    this.ratedUserId = ratedUserId;
    this.stars = stars;
    this.feedback = feedback;
    this.createdAt = LocalDateTime.now();
  }

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

  public int getRaterUserId() {
    return this.raterUserId;
  }

  public void setRaterUserId(int raterUserId) {
    this.raterUserId = raterUserId;
  }

  public int getRatedUserId() {
    return this.ratedUserId;
  }

  public void setRatedUserId(int ratedUserId) {
    this.ratedUserId = ratedUserId;
  }

  public int getStars() {
    return this.stars;
  }

  public void setStars(int stars) {
    this.stars = stars;
  }

  public String getFeedback() {
    return this.feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getRaterUsername() {
    return this.raterUsername;
  }

  public void setRaterUsername(String raterUsername) {
    this.raterUsername = raterUsername;
  }
}
