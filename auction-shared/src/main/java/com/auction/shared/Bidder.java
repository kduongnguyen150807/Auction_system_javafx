package com.auction.shared;
public class Bidder extends User {
    public Bidder() {
        super();
    }
    public Bidder(String username, String password, String email) {
        super(username, password, email);
    }
    @Override
    public String getrolename() {
        String ans = "Bidder";
        return ans;
    }
}