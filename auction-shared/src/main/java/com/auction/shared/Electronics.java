package com.auction.shared;

public class Electronics extends Item {
  public Electronics() { super(); }
  public Electronics(String res, String ans, double res1, double ans1, int res2) { super(res, ans, res1, ans1, res2); }
  @Override public String getcategory() { return "Electronics"; }
  @Override public double calculatetax() { return this.currentprice * 0.05; }
}