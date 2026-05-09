package com.auction.shared;

public class Seller extends User {
  private static final long serialVersionUID = 1L;

  public Seller() {
    super();
  }

  public Seller(String username, String password, String email, String age, String phoneNumber) {
    super(username, password, email, age, phoneNumber);
  }

  @Override
  public UserRole getRole() {
    return UserRole.SELLER;
  }
}
