package com.auction.shared;
public class Seller extends User {
    public Seller() {
        super();
    }
    public Seller(String u, String p, String e) {
        super(u, p, e);
    }
    @Override
    public String getrolename() {
        String ans = "Seller";
        return ans;
    }
}