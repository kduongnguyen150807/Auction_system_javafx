package com.auction.shared.user;

import com.auction.shared.Entity;
import com.auction.shared.UserRole;

public abstract class User extends Entity {
  protected String username;
  protected String fullName;
  protected String password;
  protected String email;
  protected String age;
  protected String phoneNumber;
  protected double balance;
  protected boolean active;
  protected boolean locked;
  protected String avatarUrl;
  protected double moneySpent;
  protected int itemsBought;
  protected double moneyReceived;
  protected int itemsSold;
  protected double avgRating;
  protected int totalRatings;

  public User() {}

  public User(String u, String p, String e, String a, String ph) {
    this.username = u;
    this.fullName = u;
    this.password = p;
    this.email = e;
    this.age = a;
    this.phoneNumber = ph;
    this.balance = 0.0;
    this.moneySpent = 0.0;
    this.itemsBought = 0;
    this.moneyReceived = 0.0;
    this.itemsSold = 0;
    this.active = true;
    this.locked = false;
  }

  public abstract UserRole getRole();

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String u) {
    this.username = u;
  }

  public String getFullName() {
    return this.fullName;
  }

  public void setFullName(String ans) {
    this.fullName = ans;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String p) {
    this.password = p;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String e) {
    this.email = e;
  }

  public String getAge() {
    return this.age;
  }

  public void setAge(String a) {
    this.age = a;
  }

  public String getPhoneNumber() {
    return this.phoneNumber;
  }

  public void setPhoneNumber(String ph) {
    this.phoneNumber = ph;
  }

  public double getBalance() {
    return this.balance;
  }

  public void setBalance(double b) {
    this.balance = b;
  }

  public boolean isActive() {
    return this.active;
  }

  public void setActive(boolean a) {
    this.active = a;
  }

  public boolean isLocked() {
    return this.locked;
  }

  public void setLocked(boolean l) {
    this.locked = l;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String ans) {
    this.avatarUrl = ans;
  }

  public double getMoneySpent() {
    return this.moneySpent;
  }

  public void setMoneySpent(double m) {
    this.moneySpent = m;
  }

  public int getItemsBought() {
    return this.itemsBought;
  }

  public void setItemsBought(int i) {
    this.itemsBought = i;
  }

  public double getMoneyReceived() {
    return this.moneyReceived;
  }

  public void setMoneyReceived(double m) {
    this.moneyReceived = m;
  }

  public int getItemsSold() {
    return this.itemsSold;
  }

  public void setItemsSold(int i) {
    this.itemsSold = i;
  }

  public double getAvgRating() {
    return this.avgRating;
  }

  public void setAvgRating(double r) {
    this.avgRating = r;
  }

  public int getTotalRatings() {
    return this.totalRatings;
  }

  public void setTotalRatings(int r) {
    this.totalRatings = r;
  }
}
