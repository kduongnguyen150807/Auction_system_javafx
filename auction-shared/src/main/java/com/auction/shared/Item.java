package com.auction.shared;

import java.time.LocalDateTime;

public abstract class Item extends Entity {
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

  public Item(String res, String ans, double res1, double ans1, int res2) {
    this.name = res;
    this.description = ans;
    this.startingprice = res1;
    this.currentprice = ans1;
    this.sellerid = res2;
    this.winnerid = -1;
    this.status = ItemStatus.OPEN;
  }

  public abstract double calculatetax();

  public String getcategory() { return this.category; }
  public void setcategory(String res) { this.category = res; }
  public String getname() { return this.name; }
  public void setname(String res) { this.name = res; }
  public String getdescription() { return this.description; }
  public void setdescription(String res) { this.description = res; }
  public double getstartingprice() { return this.startingprice; }
  public void setstartingprice(double res) { this.startingprice = res; }
  public double getcurrentprice() { return this.currentprice; }
  public void setcurrentprice(double res) { this.currentprice = res; }
  public LocalDateTime getstarttime() { return this.starttime; }
  public void setstarttime(LocalDateTime res) { this.starttime = res; }
  public LocalDateTime getendtime() { return this.endtime; }
  public void setendtime(LocalDateTime res) { this.endtime = res; }
  public double getmaxprice() { return this.maxprice; }
  public void setmaxprice(double res) { this.maxprice = res; }
  public int getsellerid() { return this.sellerid; }
  public void setsellerid(int res) { this.sellerid = res; }
  public int getwinnerid() { return this.winnerid; }
  public void setwinnerid(int res) { this.winnerid = res; }
  public ItemStatus getstatus() { return this.status; }
  public void setstatus(ItemStatus res) { this.status = res; }
  public String getimageurl() { return this.imageurl; }
  public void setimageurl(String res) { this.imageurl = res; }
  public String getsellerusername() { return this.sellerusername; }
  public void setsellerusername(String res) { this.sellerusername = res; }
  public String getselleravatarurl() { return this.selleravatarurl; }
  public void setselleravatarurl(String res) { this.selleravatarurl = res; }
}