package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Rating implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int itemid;
    private int rateruserid;
    private int rateduserid;
    private int stars;
    private String feedback;
    private LocalDateTime createdat;
    private String raterusername;

    public Rating() {}

    public Rating(int itemid, int rateruserid, int rateduserid, int stars, String feedback) {
        this.itemid = itemid;
        this.rateruserid = rateruserid;
        this.rateduserid = rateduserid;
        this.stars = stars;
        this.feedback = feedback;
        this.createdat = LocalDateTime.now();
    }

    public int getid() { return this.id; }
    public void setid(int id) { this.id = id; }
    public int getitemid() { return this.itemid; }
    public void setitemid(int res) { this.itemid = res; }
    public int getrateruserid() { return this.rateruserid; }
    public void setrateruserid(int res) { this.rateruserid = res; }
    public int getrateduserid() { return this.rateduserid; }
    public void setrateduserid(int res) { this.rateduserid = res; }
    public int getstars() { return this.stars; }
    public void setstars(int res) { this.stars = res; }
    public String getfeedback() { return this.feedback; }
    public void setfeedback(String res) { this.feedback = res; }
    public LocalDateTime getcreatedat() { return this.createdat; }
    public void setcreatedat(LocalDateTime res) { this.createdat = res; }
    public String getraterusername() { return this.raterusername; }
    public void setraterusername(String res) { this.raterusername = res; }
}
