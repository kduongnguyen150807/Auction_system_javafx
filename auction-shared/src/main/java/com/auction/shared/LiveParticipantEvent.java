package com.auction.shared;

import java.io.Serializable;

/** Server push when someone joins or leaves a live auction room. */
public class LiveParticipantEvent implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum Action {
    JOINED,
    LEFT
  }

  private int itemId;
  private int userId;
  private String username;
  private String avatarUrl;
  private Action action;
  private boolean vip;

  public LiveParticipantEvent() {}

  public LiveParticipantEvent(
      int itemId, int userId, String username, String avatarUrl, Action action) {
    this(itemId, userId, username, avatarUrl, action, false);
  }

  public LiveParticipantEvent(
      int itemId, int userId, String username, String avatarUrl, Action action, boolean vip) {
    this.itemId = itemId;
    this.userId = userId;
    this.username = username;
    this.avatarUrl = avatarUrl;
    this.action = action;
    this.vip = vip;
  }

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
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

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public boolean isVip() {
    return vip;
  }

  public void setVip(boolean vip) {
    this.vip = vip;
  }
}
