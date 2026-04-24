package com.auction.shared.Item;

public class ItemFactory {
  public static Item createItem(String res) {
    if (res == null) return new Vehicle();
    if (res.equalsIgnoreCase("Electronics")) return new Electronics();
    if (res.equalsIgnoreCase("Art")) return new Art();
    return new Vehicle();
  }
}
