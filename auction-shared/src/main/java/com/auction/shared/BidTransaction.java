package com.auction.shared;
import java.time.LocalDateTime;
public class BidTransaction extends Entity {
    private int iid;
    private int uid;
    private double val;
    private LocalDateTime ts;
    public BidTransaction() {}
    public BidTransaction(int iid, int uid, double val) {
        this.iid = iid;
        this.uid = uid;
        this.val = val;
        this.ts = LocalDateTime.now();
    }
    public int getiid() { return iid; }
    public void setiid(int id) { this.iid = id; }
    public int getuid() { return uid; }
    public void setuid(int id) { this.uid = id; }
    public double getval() { return val; }
    public void setval(double v) { this.val = v; }
    public LocalDateTime getts() { return ts; }
    public void setts(LocalDateTime t) { this.ts = t; }
}