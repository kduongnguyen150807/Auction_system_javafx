package com.auction.shared;

public class Admin extends User {
  private static final long serialVersionUID = 1L;

  public Admin() {
    super();
  }

  public Admin(String u, String p, String e, String a, String ph) {
    super(u, p, e, a, ph);
  }

  @Override
  public UserRole getRole() {
    UserRole ans = UserRole.ADMIN;
    return ans;
  }
}
