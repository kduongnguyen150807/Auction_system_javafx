package com.auction.client.store.userinformation;

import com.auction.client.util.StarUtils;
import com.auction.shared.User;
import com.auction.shared.UserRole;
import javafx.beans.property.*;

public class UserModel {
  protected User user;

  protected final StringProperty username = new SimpleStringProperty();
  protected final StringProperty fullName = new SimpleStringProperty();
  protected final StringProperty email = new SimpleStringProperty();
  protected final ObjectProperty<UserRole> role = new SimpleObjectProperty<>();
  protected final DoubleProperty moneySpent = new SimpleDoubleProperty(0);
  protected final StringProperty avatarUrl = new SimpleStringProperty();
  protected final StringProperty phoneNumber = new SimpleStringProperty();
  protected final DoubleProperty currentBalance = new SimpleDoubleProperty(0);

  protected final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper();
  protected final ReadOnlyStringWrapper avgRating = new ReadOnlyStringWrapper();

  private int rank;

  public UserModel() {
    this.user = null;
  }

  public UserModel(User user) {
    updateUser(user);
  }

  public void updateUser(User user) {
    this.user = user;
    if (user == null) {
      clearModel();
      return;
    }
    this.username.set(user.getUsername());
    this.fullName.set(user.getFullName());
    this.email.set(user.getEmail());
    this.role.set(user.getRole());
    this.moneySpent.set(user.getMoneySpent());
    this.phoneNumber.set(user.getPhoneNumber());
    this.avatarUrl.set(user.getAvatarUrl());
    this.currentBalance.set(user.getBalance());

    String statusStr = user.isLocked() ? "LOCKED" :
      (user.getAvgRating() < 2.0 && user.getTotalRatings() >= 3 ? "LOW REP" : "active");
    this.status.set(statusStr);

    String ratingStr = "N/A";
    if (user.getTotalRatings() > 0) {
      String sentiment = StarUtils.getRatingTypeFromAvg(user.getAvgRating());
      ratingStr = String.format("%.1f (%d) %s", user.getAvgRating(), user.getTotalRatings(), sentiment);
    }
    this.avgRating.set(ratingStr);
  }

  protected void clearModel() {
    this.user = null;
    this.username.set(null);
    this.fullName.set(null);
    this.email.set(null);
    this.role.set(null);
    this.moneySpent.set(0.0);
    this.phoneNumber.set(null);
    this.avatarUrl.set(null);
    this.currentBalance.set(0.0);
    this.status.set(null);
    this.avgRating.set("N/A");
  }

  // --- Getters & Properties ---
  public StringProperty usernameProperty() { return username; }
  public StringProperty fullNameProperty() { return fullName; }
  public StringProperty emailProperty() { return email; }
  public ObjectProperty<UserRole> roleProperty() { return role; }
  public DoubleProperty moneySpentProperty() { return moneySpent; }
  public StringProperty avatarUrlProperty() { return avatarUrl; }
  public StringProperty phoneNumberProperty() { return phoneNumber; }
  public DoubleProperty currentBalanceProperty() { return currentBalance; }

  public ReadOnlyStringProperty statusProperty() {
    return status.getReadOnlyProperty();
  }

  public ReadOnlyStringProperty avgRatingProperty() {
    return avgRating.getReadOnlyProperty();
  }

  public int getRank() { return rank; }
  public void setRank(int rank) { this.rank = rank; }
  public User getUser() { return user; }
}