package com.auction.shared;

public class Admin extends User {
  private static final long serialVersionUID = 1L;

  public Admin() {
    super();
  }

  public Admin(String username, String password, String email, String age, String phoneNumber) {
    super(username, password, email, age, phoneNumber);
  }

  @Override
  public UserRole getRole() {
    return UserRole.ADMIN;
  }
}
