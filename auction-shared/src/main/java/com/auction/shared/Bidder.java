package com.auction.shared;
public class Bidder extends User {
    public Bidder() { super(); }
    public Bidder(String u, String p, String e, String a, String ph) {
        super(u, p, e, a, ph);
    }
    @Override
    public UserRole getrole() {
        UserRole ans = UserRole.BIDDER;
        return ans;
    }
}