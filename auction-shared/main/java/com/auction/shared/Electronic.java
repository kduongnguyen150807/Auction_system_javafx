package com.auction.shared;
public class Electronics extends Item {
    public Electronics() {
        super();
    }
    public Electronics(String name, String description, double startingprice, double currentprice) {
        this.name = name;
        this.description = description;
        this.startingprice = startingprice;
        this.currentprice = currentprice;
    }
}