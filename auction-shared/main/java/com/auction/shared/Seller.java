package com.auction.shared;
public class Seller extends User {
    public Seller() {
        super();
    }
    public Seller(String username, String password, String email) {
        super(username, password, email);
    }
    @Override
    public String getrolename() {
        String ans = "Seller";
        return ans;
    }
}