package com.auction.shared;
public class Admin extends User {
    public Admin() { super(); }
    public Admin(String u, String p, String e, String a, String ph) {
        super(u, p, e, a, ph);
    }
    @Override
    public UserRole getrole() {
        UserRole ans = UserRole.ADMIN;
        return ans;
    }
}