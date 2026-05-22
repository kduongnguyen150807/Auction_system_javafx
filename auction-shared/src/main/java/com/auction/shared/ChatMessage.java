package com.auction.shared;

import java.time.LocalDateTime;

public class ChatMessage extends Entity {
  private static final long serialVersionUID = 1L;

  public static final String TYPE_GLOBAL = "GLOBAL";
  public static final String TYPE_PRIVATE = "PRIVATE";

  private int senderId;
  private String senderUsername;
  private String senderAvatarUrl;
  private int receiverId;
  private String receiverUsername;
  private String content;
  private String messageType;
  private LocalDateTime createdAt;
  private boolean senderVip;

  public ChatMessage() {
    this.createdAt = LocalDateTime.now();
  }

  public ChatMessage(int senderId, String senderUsername, String content, String messageType) {
    this.senderId = senderId;
    this.senderUsername = senderUsername;
    this.content = content;
    this.messageType = messageType;
    this.createdAt = LocalDateTime.now();
  }

  public int getSenderId() { return this.senderId; }
  public void setSenderId(int senderId) { this.senderId = senderId; }

  public String getSenderUsername() { return this.senderUsername; }
  public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

  public String getSenderAvatarUrl() { return this.senderAvatarUrl; }
  public void setSenderAvatarUrl(String senderAvatarUrl) { this.senderAvatarUrl = senderAvatarUrl; }

  public int getReceiverId() { return this.receiverId; }
  public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

  public String getReceiverUsername() { return this.receiverUsername; }
  public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }

  public String getContent() { return this.content; }
  public void setContent(String content) { this.content = content; }

  public String getMessageType() { return this.messageType; }
  public void setMessageType(String messageType) { this.messageType = messageType; }

  public LocalDateTime getCreatedAt() { return this.createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public boolean isSenderVip() { return senderVip; }
  public void setSenderVip(boolean senderVip) { this.senderVip = senderVip; }
}
