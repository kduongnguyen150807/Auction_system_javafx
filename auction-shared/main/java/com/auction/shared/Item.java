package com.auction.shared;
import java.time.LocalDateTime;
public abstract class Item extends Entity {
    protected String name;
    protected String description;
    protected double startingprice;
    protected double currentprice;
    protected LocalDateTime starttime;
    protected LocalDateTime endtime;
    public String getname() {
        String ans = this.name;
        return ans;
    }
    public void setname(String name) {
        this.name = name;
    }
    public String getdescription() {
        String ans = this.description;
        return ans;
    }
    public void setdescription(String description) {
        this.description = description;
    }
    public double getstartingprice() {
        double ans = this.startingprice;
        return ans;
    }
    public void setstartingprice(double startingprice) {
        this.startingprice = startingprice;
    }
    public double getcurrentprice() {
        double ans = this.currentprice;
        return ans;
    }
    public void setcurrentprice(double currentprice) {
        this.currentprice = currentprice;
    }
    public LocalDateTime getstarttime() {
        LocalDateTime ans = this.starttime;
        return ans;
    }
    public void setstarttime(LocalDateTime starttime) {
        this.starttime = starttime;
    }
    public LocalDateTime getendtime() {
        LocalDateTime ans = this.endtime;
        return ans;
    }
    public void setendtime(LocalDateTime endtime) {
        this.endtime = endtime;
    }
}