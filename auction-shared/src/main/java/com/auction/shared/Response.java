package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Response implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String OK = "SUCCESS";
  public static final String ERROR = "ERROR";
  protected String requestId;
  protected String status;
  protected String message;
  protected Object payload;
  protected LocalDateTime timestamp;

  public Response(){

  }
  public Response(String rid, String st, String msg, Object obj) {
    this.requestId = rid;
    this.status = st;
    this.message = msg;
    this.payload = obj;
    this.timestamp = LocalDateTime.now();
  }

  public String getRequestId() {
    String ans = this.requestId;
    return ans;
  }

  public String getStatus() {
    String ans = this.status;
    return ans;
  }

  public String getMessage() {
    String ans = this.message;
    return ans;
  }

  public Object getPayload() {
    Object ans = this.payload;
    return ans;
  }

  public LocalDateTime getTimestamp() {
    LocalDateTime ans = this.timestamp;
    return ans;
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
