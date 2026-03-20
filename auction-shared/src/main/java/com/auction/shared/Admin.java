package com.auction.shared;
public class Admin extends User {
    public Admin() {
        super();
    }
    public Admin(String u, String p, String e) {
        super(u, p, e);
    }
    @Override
    public String getrolename() {
        String ans = "Admin";
        return ans;
    }
}