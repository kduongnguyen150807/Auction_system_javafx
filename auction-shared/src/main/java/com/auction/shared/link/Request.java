package com.auction.shared.link;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Request implements Serializable {
  public static final String GET_ONGOING_BIDS = "GET_ONGOING_BIDS";
  protected String requestId;
  protected RequestType action;
  protected Object payload;
  protected LocalDateTime timestamp;

  public Request(RequestType act, Object obj) {
    this.requestId = UUID.randomUUID().toString();
    this.action = act;
    this.payload = obj;
    this.timestamp = LocalDateTime.now();
  }

  public String getRequestId() {
    String ans = this.requestId;
    return ans;
  }

  public RequestType getAction() {
    RequestType ans = this.action;
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
}
