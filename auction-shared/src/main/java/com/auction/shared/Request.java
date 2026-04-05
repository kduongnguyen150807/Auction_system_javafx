package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Request implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String LOGIN = "LOGIN";
  public static final String SIGNUP = "SIGNUP";
  public static final String BID = "BID";
  public static final String ADD = "ADD";
  public static final String LIST = "LIST";
  public static final String UPDATE_PROFILE = "UPDATE_PROFILE";
  public static final String UPDATE_AVATAR = "UPDATE_AVATAR";
  public static final String GET_ALL_USERS = "GET_ALL_USERS";
  public static final String LOCK_USER = "LOCK_USER";
  public static final String UNLOCK_USER = "UNLOCK_USER";
  public static final String ADD_LOT = "ADD_LOT";
  public static final String GET_ONGOING_BIDS = "GET_ONGOING_BIDS";
  public static final String GET_UPCOMING_BIDS = "GET_UPCOMING_BIDS";
  public static final String SUBMIT_RATING = "SUBMIT_RATING";
  public static final String GET_RATINGS = "GET_RATINGS";
  public static final String GET_PENDING_ITEMS = "GET_PENDING_ITEMS";
  public static final String APPROVE_ITEM = "APPROVE_ITEM";
  public static final String REJECT_ITEM = "REJECT_ITEM";
  public static final String GET_ITEM_BY_ID = "GET_ITEM_BY_ID";
  public static final String PROMOTE_ADMIN = "PROMOTE_ADMIN";
  public static final String SEARCH_USERS = "SEARCH_USERS";
  public static final String GET_USER_BY_ID = "GET_USER_BY_ID";
  protected String requestId;
  protected String action;
  protected Object payload;
  protected LocalDateTime timestamp;

  public Request(String act, Object obj) {
    this.requestId = UUID.randomUUID().toString();
    this.action = act;
    this.payload = obj;
    this.timestamp = LocalDateTime.now();
  }

  public String getRequestId() {
    String ans = this.requestId;
    return ans;
  }

  public String getAction() {
    String ans = this.action;
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
