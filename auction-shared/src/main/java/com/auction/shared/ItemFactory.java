package com.auction.shared;

public class ItemFactory {
    public static Item createitem(String res) {
        if (res == null) return new Vehicle();
        if (res.equalsIgnoreCase("Electronics")) return new Electronics();
        if (res.equalsIgnoreCase("Art")) return new Art();
        return new Vehicle();
    }
}