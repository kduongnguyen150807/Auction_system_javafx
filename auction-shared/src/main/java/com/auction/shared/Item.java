package com.auction.shared;

import java.time.LocalDateTime;

public class Item extends Entity {
  private static final long serialVersionUID = 1L;
  protected String name;
  protected String description;
  protected double startingprice;
  protected double currentprice;
  protected LocalDateTime starttime;
  protected LocalDateTime endtime;
  protected double maxprice;
  protected int sellerid;
  protected int winnerid;
  protected ItemStatus status;
  protected String imageurl;
  protected String sellerusername;
  protected String selleravatarurl;
  protected String category;

  public Item() {}

  public Item(String n, String d, double sp, double cp, int sid) {
    this.name = n;
    this.description = d;
    this.startingprice = sp;
    this.currentprice = cp;
    this.sellerid = sid;
    this.winnerid = -1;
    this.status = ItemStatus.OPEN;
  }

  // BỎ abstract ở đây và viết body bình thường
  public String getcategory() {
    return this.category;
  }

  public void setcategory(String c) {
    this.category = c;
  }

  public String getname() { return this.name; }
  public void setname(String n) { this.name = n; }

  public String getdescription() { return this.description; }
  public void setdescription(String d) { this.description = d; }

  public double getstartingprice() { return this.startingprice; }
  public void setstartingprice(double sp) { this.startingprice = sp; }

  public double getcurrentprice() { return this.currentprice; }
  public void setcurrentprice(double cp) { this.currentprice = cp; }

  public LocalDateTime getstarttime() { return this.starttime; }
  public void setstarttime(LocalDateTime st) { this.starttime = st; }

  public LocalDateTime getendtime() { return this.endtime; }
  public void setendtime(LocalDateTime et) { this.endtime = et; }

  public double getmaxprice() { return this.maxprice; }
  public void setmaxprice(double m) { this.maxprice = m; }

  public int getsellerid() { return this.sellerid; }
  public void setsellerid(int sid) { this.sellerid = sid; }

  public int getwinnerid() { return this.winnerid; }
  public void setwinnerid(int wid) { this.winnerid = wid; }

  public ItemStatus getstatus() { return this.status; }
  public void setstatus(ItemStatus s) { this.status = s; }

  public String getimageurl() { return this.imageurl; }
  public void setimageurl(String u) { this.imageurl = u; }

  public String getsellerusername() { return this.sellerusername; }
  public void setsellerusername(String su) { this.sellerusername = su; }

  public String getselleravatarurl() { return this.selleravatarurl; }
  public void setselleravatarurl(String sa) { this.selleravatarurl = sa; }
}