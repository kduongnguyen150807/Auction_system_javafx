package com.auction.shared;

import java.io.Serializable;

public class LiveParticipantSummary implements Serializable {
  private static final long serialVersionUID = 1L;

  private int userId;
  private String username;
  private String avatarUrl;

  public LiveParticipantSummary() {}

  public LiveParticipantSummary(int userId, String username, String avatarUrl) {
    this.userId = userId;
    this.username = username;
    this.avatarUrl = avatarUrl;
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
}
