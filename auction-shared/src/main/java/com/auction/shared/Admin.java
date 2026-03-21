package com.auction.shared;
public class Admin extends User {
    public Admin() { super(); }
    public Admin(String u, String p, String e, String a) {
        super(u, p, e, a);
    }
    @Override
    public String getrolename() {
        String ans = "Admin";
        return ans;
    }
}