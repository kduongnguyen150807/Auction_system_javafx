package com.auction.shared;

import java.io.Serializable;

public class LiveParticipantSummary implements Serializable {
  private static final long serialVersionUID = 1L;

  private int userId;
  private String username;
  private String avatarUrl;
  private boolean vip;

  public LiveParticipantSummary() {}

  public LiveParticipantSummary(int userId, String username, String avatarUrl) {
    this(userId, username, avatarUrl, false);
  }

  public LiveParticipantSummary(int userId, String username, String avatarUrl, boolean vip) {
    this.userId = userId;
    this.username = username;
    this.avatarUrl = avatarUrl;
    this.vip = vip;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public boolean isVip() {
    return vip;
  }

  public void setVip(boolean vip) {
    this.vip = vip;
  }
}
