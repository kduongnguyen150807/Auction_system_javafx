package com.auction.shared.dto;

import java.io.Serializable;

public class RegisterCredentials implements Serializable {
  private String username;
  private String password;
  private String confirmPassword;
  private String email;
  private int age;

  public RegisterCredentials(String username, String password, String confirmPassword, String email, int age) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.age = age;
    this.confirmPassword = confirmPassword;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getEmail() {
    return email;
  }

  public int getAge() {
    return age;
  }
}
