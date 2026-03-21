package com.auction.shared;
public class Seller extends User {
    public Seller() { super(); }
    public Seller(String u, String p, String e, String a) {
        super(u, p, e, a);
    }
    @Override
    public String getrolename() {
        String ans = "Seller";
        return ans;
    }
}