package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class User extends Entity implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String username;
  protected String fullName;
  protected String password;
  protected String email;
  protected String age;
  protected String phoneNumber;

  protected double balance;

  protected boolean active;
  protected boolean locked;
  protected boolean verified;

  protected String avatarUrl;

  protected String sessionToken;
  protected boolean online;
  protected LocalDateTime lastLogin;
  protected String deviceId;

  protected double moneySpent;
  protected int itemsBought;

  protected double moneyReceived;
  protected int itemsSold;

  protected double avgRating;
  protected int totalRatings;

  protected LocalDateTime vipUntil;

  protected LocalDateTime lastDailySpinAt;
  protected int paidSpinCredits;

  public User() {

    this.balance = 0.0;

    this.moneySpent = 0.0;
    this.itemsBought = 0;

    this.moneyReceived = 0.0;
    this.itemsSold = 0;

    this.avgRating = 0.0;
    this.totalRatings = 0;

    this.active = true;
    this.locked = false;
    this.verified = false;

    this.online = false;
  }

  public User(
          String username,
          String password,
          String email,
          String age,
          String phoneNumber) {

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

    this.avgRating = 0.0;
    this.totalRatings = 0;

    this.active = true;
    this.locked = false;
    this.verified = false;

    this.online = false;
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

  public boolean isVerified() {
    return this.verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public String getAvatarUrl() {
    return this.avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getSessionToken() {
    return this.sessionToken;
  }

  public void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }

  public boolean isOnline() {
    return this.online;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }

  public LocalDateTime getLastLogin() {
    return this.lastLogin;
  }

  public void setLastLogin(LocalDateTime lastLogin) {
    this.lastLogin = lastLogin;
  }

  public String getDeviceId() {
    return this.deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
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

  public LocalDateTime getVipUntil() {
    return vipUntil;
  }

  public void setVipUntil(LocalDateTime vipUntil) {
    this.vipUntil = vipUntil;
  }

  /** Active VIP if expiry is in the future. */
  public boolean isVip() {
    return vipUntil != null && vipUntil.isAfter(LocalDateTime.now());
  }

  public LocalDateTime getLastDailySpinAt() {
    return lastDailySpinAt;
  }

  public void setLastDailySpinAt(LocalDateTime lastDailySpinAt) {
    this.lastDailySpinAt = lastDailySpinAt;
  }

  public int getPaidSpinCredits() {
    return paidSpinCredits;
  }

  public void setPaidSpinCredits(int paidSpinCredits) {
    this.paidSpinCredits = paidSpinCredits;
  }

  public void deposit(double amount) {
    this.balance += amount;
  }

  public void withdraw(double amount) {
    this.balance -= amount;
  }

  public void addMoneySpent(double amount) {
    this.moneySpent += amount;
  }

  public void incrementItemsBought() {
    this.itemsBought++;
  }

  public void addMoneyReceived(double amount) {
    this.moneyReceived += amount;
  }

  public void incrementItemsSold() {
    this.itemsSold++;
  }

  public void updateRating(double newRating) {

    double totalScore = this.avgRating * this.totalRatings;

    this.totalRatings++;

    this.avgRating =
            (totalScore + newRating) / this.totalRatings;
  }
}