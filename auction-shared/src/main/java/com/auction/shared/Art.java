package com.auction.shared;
public class Art extends Item {
    public Art() { super(); }
    public Art(String n, String d, double sp, double cp, int sid) {
        super(n, d, sp, cp, sid);
    }
    @Override
    public String getcategory() {
        String ans = "Art";
        return ans;
    }
}