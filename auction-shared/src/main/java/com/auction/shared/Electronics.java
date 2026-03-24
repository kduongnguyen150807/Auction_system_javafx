package com.auction.shared;
public class Electronics extends Item {
    public Electronics() { super(); }
    public Electronics(String n, String d, double sp, double cp, int sid) {
        super(n, d, sp, cp, sid);
    }
    @Override
    public String getcategory() {
        String ans = "Electronics";
        return ans;
    }
}