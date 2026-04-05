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
  protected double autobidincrement; //mặc định autobid tăng bước nhảy là 10$

  public BidTransaction() {
    super();
    this.timestamp = LocalDateTime.now();
    this.autobidincrement = 10.0;
  }

  public BidTransaction(int res, int ans, double res1) {
    super();
    this.itemid = res;
    this.userid = ans;
    this.bidvalue = res1;
    this.timestamp = LocalDateTime.now();
    this.maxautobid = 0.0;
    this.isautobid = false;
    this.autobidincrement = 10.0;
  }

  public int getitemid() { return itemid; }
  public void setitemid(int res) { this.itemid = res; }
  public int getuserid() { return userid; }
  public void setuserid(int ans) { this.userid = ans; }
  public double getbidvalue() { return bidvalue; }
  public void setbidvalue(double res) { this.bidvalue = res; }
  public LocalDateTime gettimestamp() { return timestamp; }
  public void settimestamp(LocalDateTime ans) { this.timestamp = ans; }
  public double getmaxautobid() { return maxautobid; }
  public void setmaxautobid(double res) { this.maxautobid = res; }
  public boolean getisautobid() { return isautobid; }
  public void setisautobid(boolean ans) { this.isautobid = ans; }
  public double getautobidincrement() { return autobidincrement; }
  public void setautobidincrement(double res) { this.autobidincrement = res; }
}