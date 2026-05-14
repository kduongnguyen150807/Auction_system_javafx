package com.auction.shared;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDateTime;

// Tính năng 1: Giúp Jackson biết chính xác lớp con nào để khởi tạo khi nhận JSON
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Electronics.class, name = "electronics"),
        @JsonSubTypes.Type(value = Art.class, name = "art"),
        @JsonSubTypes.Type(value = Vehicle.class, name = "vehicle")
})
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
  protected String sessiontoken;

  public User() {}

  public User(String username, String password, String email, String age, String phoneNumber) {
    this.username = username;
    this.fullName = username;
    this.password = password;
    this.email = email;
    this.age = age;
    this.phoneNumber = phoneNumber;
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

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFullName() {
    return this.fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAge() {
    return this.age;
  }

  public void setAge(String age) {
    this.age = age;
  }

  public String getPhoneNumber() {
    return this.phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public double getBalance() {
    return this.balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }

  public boolean isActive() {
    return this.active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isLocked() {
    return this.locked;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public String getAvatarUrl() {
    return this.avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public double getMoneySpent() {
    return this.moneySpent;
  }

  public void setMoneySpent(double moneySpent) {
    this.moneySpent = moneySpent;
  }

  public int getItemsBought() {
    return this.itemsBought;
  }

  public void setItemsBought(int itemsBought) {
    this.itemsBought = itemsBought;
  }

  public double getMoneyReceived() {
    return this.moneyReceived;
  }

  public void setMoneyReceived(double moneyReceived) {
    this.moneyReceived = moneyReceived;
  }

  public int getItemsSold() {
    return this.itemsSold;
  }

  public void setItemsSold(int itemsSold) {
    this.itemsSold = itemsSold;
  }

  public double getAvgRating() {
    return this.avgRating;
  }

  public void setAvgRating(double avgRating) {
    this.avgRating = avgRating;
  }

  public int getTotalRatings() {
    return this.totalRatings;
  }

  public void setTotalRatings(int totalRatings) {
    this.totalRatings = totalRatings;
  }

  public String getSessiontoken() {
    return this.sessiontoken;
  }

  public void setSessiontoken(String sessiontoken) {
    this.sessiontoken = sessiontoken;
  }
}