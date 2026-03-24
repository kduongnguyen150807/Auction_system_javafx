package com.auction.shared;
import java.time.LocalDateTime;
public class BidTransaction extends Entity {
    protected int itemid;
    protected int userid;
    protected double bidvalue;
    protected LocalDateTime timestamp;
    public BidTransaction() { super(); }
    public BidTransaction(int iid, int uid, double val) {
        super();
        this.itemid = iid;
        this.userid = uid;
        this.bidvalue = val;
        this.timestamp = LocalDateTime.now();
    }
    public int getitemid() { int ans = this.itemid; return ans; }
    public void setitemid(int id) { this.itemid = id; }
    public int getuserid() { int ans = this.userid; return ans; }
    public void setuserid(int id) { this.userid = id; }
    public double getbidvalue() { double ans = this.bidvalue; return ans; }
    public void setbidvalue(double v) { this.bidvalue = v; }
    public LocalDateTime gettimestamp() { LocalDateTime ans = this.timestamp; return ans; }
    public void settimestamp(LocalDateTime t) { this.timestamp = t; }
}