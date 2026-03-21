package com.auction.shared;
public class Bidder extends User {
    public Bidder() { super(); }
    public Bidder(String u, String p, String e, String a) {
        super(u, p, e, a);
    }
    @Override
    public String getrolename() {
        String ans = "Bidder";
        return ans;
    }
}