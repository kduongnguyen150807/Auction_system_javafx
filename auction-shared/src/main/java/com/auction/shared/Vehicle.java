package com.auction.shared;
public class Vehicle extends Item {
    public Vehicle() { super(); }
    public Vehicle(String name, String description, double startingprice, double currentprice) {
        super(name, description, startingprice, currentprice);
    }
}