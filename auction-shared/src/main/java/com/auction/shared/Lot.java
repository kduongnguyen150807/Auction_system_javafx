package com.auction.shared;
import java.io.Serializable;
import java.time.LocalDateTime;
public class Lot implements Serializable {
private String title;
private String description;
private double startprice;
private LocalDateTime endtime;
private String sellerusername;
private String imageurl;
public Lot() {}
public Lot(String title, String description, double startprice, LocalDateTime endtime, String sellerusername, String imageurl) {
this.title = title;
this.description = description;
this.startprice = startprice;
this.endtime = endtime;
this.sellerusername = sellerusername;
this.imageurl = imageurl;
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
