package com.auction.shared.user;

public class Seller extends User {
  private static final long serialVersionUID = 1L;

  public Seller() {
    super();
  }

  public Seller(String u, String p, String e, int a, String ph) {
    super(u, p, e, a, ph);
  }

  @Override
  public UserRole getRole() {
    UserRole ans = UserRole.SELLER;
    return ans;
  }
}
