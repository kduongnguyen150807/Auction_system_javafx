package com.auction.shared;
public class Admin extends User {
    public Admin() {
        super();
    }
    public Admin(String username, String password, String email) {
        super(username, password, email);
    }
    @Override
    public String getrolename() {
        String ans = "Admin";
        return ans;
    }
}