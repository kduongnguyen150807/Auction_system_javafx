package com.auction.shared;
public class Vehicle extends Item {
    public Vehicle() { super(); }
    public Vehicle(String n, String d, double sp, double cp, int sid) {
        super(n, d, sp, cp, sid);
    }
    @Override
    public String getcategory() {
        String ans = "Vehicle";
        return ans;
    }
}