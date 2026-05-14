package com.auction.client.ui.utils;

public class StringFormat {
  private static final String MONEY_FORMAT = "%,.2f $";

  public static String formatMoney(double money) {
    return String.format(MONEY_FORMAT, money);
  }
}
