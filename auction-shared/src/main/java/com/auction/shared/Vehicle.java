package com.auction.shared;
public class Vehicle extends Item {
    public Vehicle() {
        super();
    }
    public Vehicle(String name, String description, double startingprice, double currentprice) {
        this.name = name;
        this.description = description;
        this.startingprice = startingprice;
        this.currentprice = currentprice;
    }
}