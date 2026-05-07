package com.auction.shared.item;

public class ItemFactory {
  public static Item createItem(ItemType type) {
    if (type == null) return new Vehicle();
    if (type.equals(ItemType.ELECTRONICS)) return new Electronics();
    if (type.equals(ItemType.ART)) return new Art();
    return new Vehicle();
  }
}
