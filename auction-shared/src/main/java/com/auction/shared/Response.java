package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Response implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final String OK = "SUCCESS";
  public static final String ERROR = "ERROR";

  /** Server-push event type: sent to a user whose account has just been locked by an admin. */
  public static final String ACCOUNT_BANNED = "ACCOUNT_BANNED";

  /** Server-push event type: sent to a user whose account has just been unlocked by an admin. */
  public static final String ACCOUNT_UNBANNED = "ACCOUNT_UNBANNED";

  protected String requestId;
  protected String status;
  protected String message;
  protected Object payload;
  protected LocalDateTime timestamp;

  public Response() {
    this.timestamp = LocalDateTime.now();
  }

  public Response(String requestId, String status, String message, Object payload) {
    this.requestId = requestId;
    this.status = status;
    this.message = message;
    this.payload = payload;
    this.timestamp = LocalDateTime.now();
  }

  public String getRequestId() {
    return this.requestId;
  }

  public String getStatus() {
    return this.status;
  }

  public String getMessage() {
    return this.message;
  }

  public Object getPayload() {
    return this.payload;
  }

  public LocalDateTime getTimestamp() {
    return this.timestamp;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setPayload(Object payload) {
    this.payload = payload;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }
}