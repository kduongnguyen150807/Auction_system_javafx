package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Response implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String OK = "SUCCESS";
  public static final String ERROR = "ERROR";

  public static final String ACCOUNT_BANNED = "ACCOUNT_BANNED";
  public static final String ACCOUNT_UNBANNED = "ACCOUNT_UNBANNED";

  protected String requestId;
  protected String status;
  protected String message;
  protected Object payload;
  protected LocalDateTime timestamp;

  // BẮT BUỘC PHẢI CÓ CHO JACKSON
  public Response() {}

  public Response(String requestId, String status, String message, Object payload) {
    this.requestId = requestId;
    this.status = status;
    this.message = message;
    this.payload = payload;
    this.timestamp = LocalDateTime.now();
  }

  public String getRequestId() { return this.requestId; }
  public void setRequestId(String requestId) { this.requestId = requestId; } // Thêm Setter

  public String getStatus() { return this.status; }
  public void setStatus(String status) { this.status = status; } // Thêm Setter

  public String getMessage() { return this.message; }
  public void setMessage(String message) { this.message = message; } // Thêm Setter

  public Object getPayload() { return this.payload; }
  public void setPayload(Object payload) { this.payload = payload; } // Thêm Setter

  public LocalDateTime getTimestamp() { return this.timestamp; }
  public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; } // Thêm Setter
}