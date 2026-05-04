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
}
