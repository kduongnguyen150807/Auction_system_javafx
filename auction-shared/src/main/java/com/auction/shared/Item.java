package com.auction.shared;
import java.time.LocalDateTime;
public abstract class Item extends Entity {
    protected String name;
    protected String description;
    protected double startingprice;
    protected double currentprice;
    protected LocalDateTime starttime;
    protected LocalDateTime endtime;
    protected int sellerid;
    protected int winnerid;
    protected ItemStatus status;
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
    public abstract String getcategory();
    public String getname() { String ans = this.name; return ans; }
    public void setname(String n) { this.name = n; }
    public String getdescription() { String ans = this.description; return ans; }
    public void setdescription(String d) { this.description = d; }
    public double getstartingprice() { double ans = this.startingprice; return ans; }
    public void setstartingprice(double sp) { this.startingprice = sp; }
    public double getcurrentprice() { double ans = this.currentprice; return ans; }
    public void setcurrentprice(double cp) { this.currentprice = cp; }
    public LocalDateTime getstarttime() { LocalDateTime ans = this.starttime; return ans; }
    public void setstarttime(LocalDateTime st) { this.starttime = st; }
    public LocalDateTime getendtime() { LocalDateTime ans = this.endtime; return ans; }
    public void setendtime(LocalDateTime et) { this.endtime = et; }
    public int getsellerid() { int ans = this.sellerid; return ans; }
    public void setsellerid(int sid) { this.sellerid = sid; }
    public int getwinnerid() { int ans = this.winnerid; return ans; }
    public void setwinnerid(int wid) { this.winnerid = wid; }
    public ItemStatus getstatus() { ItemStatus ans = this.status; return ans; }
    public void setstatus(ItemStatus s) { this.status = s; }
}