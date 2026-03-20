package com.auction.shared;
public class Art extends Item {
    public Art() {
        super();
    }
    public Art(String name, String description, double startingprice, double currentprice) {
        this.name = name;
        this.description = description;
        this.startingprice = startingprice;
        this.currentprice = currentprice;
    }
}