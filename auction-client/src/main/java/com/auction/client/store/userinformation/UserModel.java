package com.auction.client.store.userinformation;

import com.auction.client.util.StarUtils;
import com.auction.shared.User;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserModel {
  private final User user;

  private final StringProperty username;
  private final StringProperty email;
  private final StringProperty role;
  private final StringProperty status;
  private final StringProperty avgRating;

  public UserModel(User user) {
    this.user = user;

    this.username = new SimpleStringProperty(user.getUsername());
    this.email = new SimpleStringProperty(user.getEmail());
    this.role = new SimpleStringProperty(user.getRole().name());
    String statusStr = user.isLocked() ? "LOCKED" : (user.getAvgRating() < 2.0 && user.getTotalRatings() >= 3 ? "LOW REP" : "active");
    this.status = new ReadOnlyStringWrapper(statusStr);

    String ratingStr = "N/A";
    if (user.getTotalRatings() > 0) {
      String sentiment = StarUtils.getRatingTypeFromAvg(user.getAvgRating());
      ratingStr = String.format("%.1f (%d) %s", user.getAvgRating(), user.getTotalRatings(), sentiment);
    }
    this.avgRating = new ReadOnlyStringWrapper(ratingStr);
  }

  public void updateUser(User user) {
    this.username.set(user.getUsername());
    this.email.set(user.getEmail());
    this.role.set(user.getRole().name());
    this.status.set(user.isLocked() ? "LOCKED" : "active");

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
  public StringProperty statusProperty() { return status; }
  public StringProperty avgRatingProperty() { return avgRating; }

  public User getUser() {
    return user;
  }
}
