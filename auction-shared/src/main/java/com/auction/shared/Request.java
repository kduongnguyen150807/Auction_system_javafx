package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Request implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String login = "LOGIN";
  public static final String signup = "SIGNUP";
  public static final String bid = "BID";
  public static final String add = "ADD";
  public static final String list = "LIST";
  public static final String updateprofile = "UPDATE_PROFILE";
  public static final String updateavatar = "UPDATE_AVATAR";
  public static final String getallusers = "GET_ALL_USERS";
  public static final String lockuser = "LOCK_USER";
  public static final String unlockuser = "UNLOCK_USER";
  public static final String addlot = "ADD_LOT";
  public static final String getongoingbids = "GET_ONGOING_BIDS";
  public static final String getupcomingbids = "GET_UPCOMING_BIDS";
  public static final String submitrating = "SUBMIT_RATING";
  public static final String getratings = "GET_RATINGS";
  public static final String getpendingitems = "GET_PENDING_ITEMS";
  public static final String approveitem = "APPROVE_ITEM";
  public static final String rejectitem = "REJECT_ITEM";
  public static final String getitembyid = "GET_ITEM_BY_ID";
  public static final String promoteadmin = "PROMOTE_ADMIN";
  protected String requestid;
  protected String action;
  protected Object payload;
  protected LocalDateTime timestamp;

  public Request(String act, Object obj) {
    this.requestid = UUID.randomUUID().toString();
    this.action = act;
    this.payload = obj;
    this.timestamp = LocalDateTime.now();
  }

  public String getrequestid() {
    String ans = this.requestid;
    return ans;
  }

  public String getaction() {
    String ans = this.action;
    return ans;
  }

  public Object getpayload() {
    Object ans = this.payload;
    return ans;
  }

  public LocalDateTime gettimestamp() {
    LocalDateTime ans = this.timestamp;
    return ans;
  }
}
