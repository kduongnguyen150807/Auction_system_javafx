package com.auction.shared;

import java.time.LocalDateTime;

public class BidTransaction extends Entity {
  private static final long serialVersionUID = 1L;
  protected int itemid;
  protected int userid;
  protected double bidvalue;
  protected LocalDateTime timestamp;
  protected double maxautobid;
  protected boolean isautobid;

  public BidTransaction() {
    super();
  }

  public BidTransaction(int iid, int uid, double val) {
    super();
    this.itemid = iid;
    this.userid = uid;
    this.bidvalue = val;
    this.timestamp = LocalDateTime.now();
    this.maxautobid = 0.0;
    this.isautobid = false;
  }

  public int getitemid() {
    int ans = this.itemid;
    return ans;
  }

  public void setitemid(int res) {
    this.itemid = res;
  }

  public int getuserid() {
    int ans = this.userid;
    return ans;
  }

  public void setuserid(int res) {
    this.userid = res;
  }

  public double getbidvalue() {
    double ans = this.bidvalue;
    return ans;
  }

  public void setbidvalue(double res) {
    this.bidvalue = res;
  }

  public LocalDateTime gettimestamp() {
    LocalDateTime ans = this.timestamp;
    return ans;
  }

  public void settimestamp(LocalDateTime res) {
    this.timestamp = res;
  }

  public double getmaxautobid() {
    double ans = this.maxautobid;
    return ans;
  }

  public void setmaxautobid(double res) {
    this.maxautobid = res;
  }

  public boolean getisautobid() {
    boolean ans = this.isautobid;
    return ans;
  }

  public void setisautobid(boolean res) {
    this.isautobid = res;
  }
}