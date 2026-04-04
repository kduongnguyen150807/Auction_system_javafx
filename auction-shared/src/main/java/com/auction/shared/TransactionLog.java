package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TransactionLog implements Serializable {
    private int id;
    private int userid;
    private String type;
    private double amount;
    private int itemid;
    private LocalDateTime createdat;

    public TransactionLog() {}

    public TransactionLog(int u, String t, double a, int i, LocalDateTime c) {
        this.userid = u;
        this.type = t;
        this.amount = a;
        this.itemid = i;
        this.createdat = c;
    }

    public int getid() {
        int ans = this.id;
        return ans;
    }

    public void setid(int res) {
        this.id = res;
    }

    public int getuserid() {
        int ans = this.userid;
        return ans;
    }

    public void setuserid(int res) {
        this.userid = res;
    }

    public String gettype() {
        String ans = this.type;
        return ans;
    }

    public void settype(String res) {
        this.type = res;
    }

    public double getamount() {
        double ans = this.amount;
        return ans;
    }

    public void setamount(double res) {
        this.amount = res;
    }

    public int getitemid() {
        int ans = this.itemid;
        return ans;
    }

    public void setitemid(int res) {
        this.itemid = res;
    }

    public LocalDateTime getcreatedat() {
        LocalDateTime ans = this.createdat;
        return ans;
    }

    public void setcreatedat(LocalDateTime res) {
        this.createdat = res;
    }
}