package com.auction.shared;
public class Bidder extends User {
    public Bidder() {
        super();
    }
    public Bidder(String u, String p, String e) {
        super(u, p, e);
    }
    @Override
    public String getrolename() {
        String ans = "Bidder";
        return ans;
    }
}