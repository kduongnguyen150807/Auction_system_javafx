package com.auction.shared;
import java.io.Serializable;
import java.time.LocalDateTime;
public class Lot implements Serializable {
    private String title;
    private String description;
    private double startprice;
    private double bidvalue;
    private LocalDateTime starttime;
    private LocalDateTime endtime;
    private String sellerusername;
    private String imageurl;
    private int id;
    public Lot() {}
    public Lot(String title, String description, double startprice, double bidvalue, LocalDateTime starttime, LocalDateTime endtime, String sellerusername, String imageurl) {
        this.title = title;
        this.description = description;
        this.startprice = startprice;
        this.bidvalue = bidvalue;
        this.starttime = starttime;
        this.endtime = endtime;
        this.sellerusername = sellerusername;
        this.imageurl = imageurl;
    }
    public int getid() {
        int ans = this.id;
        return ans;
    }
    public void setid(int id) {
        this.id = id;
    }
    public String gettitle() {
        String ans = this.title;
        return ans;
    }
    public void settitle(String title) {
        this.title = title;
    }
    public String getdescription() {
        String ans = this.description;
        return ans;
    }
    public void setdescription(String description) {
        this.description = description;
    }
    public double getstartprice() {
        double ans = this.startprice;
        return ans;
    }
    public void setstartprice(double startprice) {
        this.startprice = startprice;
    }
    public double getbidvalue() {
        double ans = this.bidvalue;
        return ans;
    }
    public void setbidvalue(double bidvalue) {
        this.bidvalue = bidvalue;
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
    public String getsellerusername() {
        String ans = this.sellerusername;
        return ans;
    }
    public void setsellerusername(String sellerusername) {
        this.sellerusername = sellerusername;
    }
    public String getimageurl() {
        String ans = this.imageurl;
        return ans;
    }
    public void setimageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
