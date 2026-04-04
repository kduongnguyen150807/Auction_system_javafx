package com.auction.shared;

public class Seller extends User {
  private static final long serialVersionUID = 1L;
  public Seller() {
    super();
  }

  public Seller(String u, String p, String e, String a, String ph) {
    super(u, p, e, a, ph);
  }

  @Override
  public UserRole getrole() {
    UserRole ans = UserRole.SELLER;
    return ans;
  }
}
