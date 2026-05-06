package com.auction.shared.user;

import com.auction.shared.UserRole;

public class Bidder extends User {
  private static final long serialVersionUID = 1L;

  public Bidder() {
    super();
  }

  public Bidder(String u, String p, String e, String a, String ph) {
    super(u, p, e, a, ph);
  }

  @Override
  public UserRole getRole() {
    UserRole ans = UserRole.BIDDER;
    return ans;
  }
}
