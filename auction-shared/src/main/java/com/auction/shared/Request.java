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
  public static final String GET_TRENDING_LOTS = "GET_TRENDING_LOTS";
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
  public static final String GET_ONGOING_LOTS = "GET_ONGOING_LOTS";
  public static final String GET_MY_ITEMS = "get_my_items";
  public static final String GET_CLOSED_BIDS = "getclosedbids";
  public static final String GET_PAST_BIDS = "getpastbids";
  public static final String DEPOSIT = "deposit";
  public static final String REFRESH_USER = "refresh_user";
  public static final String GET_TRANSACTIONS = "get_transactions";
  public static final String GET_STATUS_STATS = "get_status_stats";
  public static final String GET_CATEGORY_STATS = "get_category_stats";
  public static final String GET_BID_HISTORY = "get_bid_history";
  public static final String PING = "ping";
  public static final String SEND_CHAT = "send_chat";
  public static final String GET_GLOBAL_CHAT_HISTORY = "get_global_chat_history";
  public static final String GET_PRIVATE_CHAT_HISTORY = "get_private_chat_history";
  public static final String GET_CHAT_CONTACTS = "get_chat_contacts";
  public static final String ADD_FRIEND = "add_friend";
  public static final String ACCEPT_FRIEND = "accept_friend";
  public static final String DECLINE_FRIEND = "decline_friend";
  public static final String REMOVE_FRIEND = "remove_friend";
  public static final String GET_FRIENDS = "get_friends";
  public static final String GET_FRIEND_REQUESTS = "get_friend_requests";
  public static final String GET_LEADERBOARD = "GET_LEADERBOARD";
  public static final String AUTOCOMPLETE = "AUTOCOMPLETE";
  public static final String RECONNECT = "RECONNECT";

  protected String requestId;
  protected String action;
  protected Object payload;
  protected LocalDateTime timestamp;

  // BẮT BUỘC PHẢI CÓ CHO JACKSON
  public Request() {}

  public Request(String action, Object payload) {
    this.requestId = UUID.randomUUID().toString();
    this.action = action;
    this.payload = payload;
    this.timestamp = LocalDateTime.now();
  }

  public String getRequestId() { return this.requestId; }
  public void setRequestId(String requestId) { this.requestId = requestId; } // Thêm Setter

  public String getAction() { return this.action; }
  public void setAction(String action) { this.action = action; } // Thêm Setter

  public Object getPayload() { return this.payload; }
  public void setPayload(Object payload) { this.payload = payload; } // Thêm Setter

  public LocalDateTime getTimestamp() { return this.timestamp; }
  public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; } // Thêm Setter
}