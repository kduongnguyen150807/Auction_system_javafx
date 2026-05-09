package com.auction.shared;

import java.time.LocalDateTime;

public class Friendship extends Entity {
  private static final long serialVersionUID = 1L;

  public static final String PENDING = "PENDING";
  public static final String ACCEPTED = "ACCEPTED";

  private int requesterId;
  private String requesterUsername;
  private String requesterAvatarUrl;
  private int addresseeId;
  private String addresseeUsername;
  private String addresseeAvatarUrl;
  private String status;
  private LocalDateTime createdAt;

  public Friendship() {
    this.createdAt = LocalDateTime.now();
  }

  public int getRequesterId() { return this.requesterId; }
  public void setRequesterId(int requesterId) { this.requesterId = requesterId; }

  public String getRequesterUsername() { return this.requesterUsername; }
  public void setRequesterUsername(String requesterUsername) { this.requesterUsername = requesterUsername; }

  public String getRequesterAvatarUrl() { return this.requesterAvatarUrl; }
  public void setRequesterAvatarUrl(String requesterAvatarUrl) { this.requesterAvatarUrl = requesterAvatarUrl; }

  public int getAddresseeId() { return this.addresseeId; }
  public void setAddresseeId(int addresseeId) { this.addresseeId = addresseeId; }

  public String getAddresseeUsername() { return this.addresseeUsername; }
  public void setAddresseeUsername(String addresseeUsername) { this.addresseeUsername = addresseeUsername; }

  public String getAddresseeAvatarUrl() { return this.addresseeAvatarUrl; }
  public void setAddresseeAvatarUrl(String addresseeAvatarUrl) { this.addresseeAvatarUrl = addresseeAvatarUrl; }

  public String getStatus() { return this.status; }
  public void setStatus(String status) { this.status = status; }

  public LocalDateTime getCreatedAt() { return this.createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
