package com.auction.client.store.userinformation;

import com.auction.client.util.StarUtils;
import com.auction.shared.User;
import javafx.beans.property.*;

public class UserModel {
  private final User user;

  private final StringProperty username = new SimpleStringProperty();
  private final StringProperty email = new SimpleStringProperty();
  private final StringProperty role = new SimpleStringProperty();
  private final DoubleProperty moneySpent = new SimpleDoubleProperty(0);
  private final StringProperty avatarUrl = new SimpleStringProperty();

  private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper();
  private final ReadOnlyStringWrapper avgRating = new ReadOnlyStringWrapper();

  private int rank;

  public UserModel(User user) {
    this.user = user;
    updateUser(user);
  }

  public void updateUser(User user) {
    this.username.set(user.getUsername());
    this.email.set(user.getEmail());
    this.role.set(user.getRole() != null ? user.getRole().name() : "");
    this.moneySpent.set(user.getMoneySpent());

    this.avatarUrl.set(user.getAvatarUrl());

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

  public StringProperty usernameProperty() { return username; }
  public StringProperty emailProperty() { return email; }
  public StringProperty roleProperty() { return role; }
  public DoubleProperty moneySpentProperty() { return moneySpent; }
  public StringProperty avatarUrlProperty() { return avatarUrl; }

  public ReadOnlyStringProperty statusProperty() {
    return status.getReadOnlyProperty();
  }

  public ReadOnlyStringProperty avgRatingProperty() {
    return avgRating.getReadOnlyProperty();
  }

  public int getRank() { return rank; }
  public void setRank(int rank) { this.rank = rank; }
  public User getUser() {
    return user;
  }
}