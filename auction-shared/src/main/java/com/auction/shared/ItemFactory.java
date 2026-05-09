package com.auction.shared;

public class ItemFactory {
  public static Item createItem(String category) {
    if (category == null) return new Vehicle();
    if (category.equalsIgnoreCase("Electronics")) return new Electronics();
    if (category.equalsIgnoreCase("Art")) return new Art();
    return new Vehicle();
  }
}