package com.auction.shared;

public class Bidder extends User {
  private static final long serialVersionUID = 1L;

  public Bidder() {
    super();
  }

  public Bidder(String username, String password, String email, String age, String phoneNumber) {
    super(username, password, email, age, phoneNumber);
  }

  @Override
  public UserRole getRole() {
    return UserRole.BIDDER;
  }
}
