package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Lot implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int itemid;
    private String title;
    private String description;
    private double bidvalue;
    private LocalDateTime starttime;
    private LocalDateTime endtime;
    private String imageurl;
    private String sellerusername;
    private String selleravatarurl;
    private String winnerusername;

    public Lot() {}

    public int getid() { return id; }
    public void setid(int i) { this.id = i; }
    public int getitemid() { return itemid; }
    public void setitemid(int i) { this.itemid = i; }
    public String gettitle() { return title; }
    public void settitle(String t) { this.title = t; }
    public String getdescription() { return description; }
    public void setdescription(String d) { this.description = d; }
    public double getbidvalue() { return bidvalue; }
    public void setbidvalue(double v) { this.bidvalue = v; }
    public LocalDateTime getstarttime() { return starttime; }
    public void setstarttime(LocalDateTime t) { this.starttime = t; }
    public LocalDateTime getendtime() { return endtime; }
    public void setendtime(LocalDateTime t) { this.endtime = t; }
    public String getimageurl() { return imageurl; }
    public void setimageurl(String u) { this.imageurl = u; }
    public String getsellerusername() { return this.sellerusername; }
    public void setsellerusername(String s) { this.sellerusername = s; }
    public String getselleravatarurl() { return this.selleravatarurl; }
    public void setselleravatarurl(String s) { this.selleravatarurl = s; }
    public String getwinnerusername() { return this.winnerusername; }
    public void setwinnerusername(String w) { this.winnerusername = w; }
}