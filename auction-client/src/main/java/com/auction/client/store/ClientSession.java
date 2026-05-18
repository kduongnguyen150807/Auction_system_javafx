package com.auction.client.store;

import com.auction.shared.User;
import com.auction.shared.UserRole;
import javafx.beans.property.*;

public class ClientSession {
  public static ClientSession CURRENT_SESSION = new ClientSession();

  private ObjectProperty<User> currentUser = new SimpleObjectProperty<>();

  private ObjectProperty<UserRole> currentRole = new SimpleObjectProperty<>();

  private SimpleDoubleProperty currentBalance = new SimpleDoubleProperty();

  private SimpleStringProperty currentName = new SimpleStringProperty();

  private SimpleStringProperty phoneNumber = new SimpleStringProperty();

  private SimpleStringProperty email = new SimpleStringProperty();

  private SimpleStringProperty avatarUrl =  new SimpleStringProperty();

  private SimpleIntegerProperty itemsSold =  new SimpleIntegerProperty();

  private SimpleDoubleProperty moneyReceived = new SimpleDoubleProperty();

  private SimpleIntegerProperty itemsBought = new SimpleIntegerProperty();

  private SimpleDoubleProperty moneySpent = new SimpleDoubleProperty();

  private SimpleDoubleProperty averageRating = new SimpleDoubleProperty();

  private ClientSession() {}

  public void setUser(User user) {
    this.currentUser.setValue(user);
    this.currentRole.setValue(user.getRole());
    this.currentBalance.setValue(user.getBalance());
    this.currentName.setValue(user.getFullName());
    this.phoneNumber.setValue(user.getPhoneNumber());
    this.email.setValue(user.getEmail());
    this.avatarUrl.setValue(user.getAvatarUrl());
    this.itemsSold.setValue(user.getItemsSold());
    this.moneyReceived.setValue(user.getMoneyReceived());
    this.itemsBought.setValue(user.getItemsBought());
    this.moneySpent.setValue(user.getMoneySpent());
    this.averageRating.setValue(user.getAvgRating());
  }

  public void applyProfileUpdate(String fullname, String phoneNumber, String email) {
    this.currentName.setValue(fullname);
    this.phoneNumber.setValue(phoneNumber);
    this.email.setValue(email);
  }

  public void applyAvatarUrl(String avatarUrl) {
    this.avatarUrl.setValue(avatarUrl);
  }

  public void deposit(double newBalance) {
    this.currentBalance.setValue(this.currentBalance.getValue() + newBalance);
  }

  public ClientSession getCurrentSession() {
    return CURRENT_SESSION;
  }

  public User getCurrentUser() {
    return currentUser.get();
  }

  public ObjectProperty<User> currentUserProperty() {
    return currentUser;
  }

  public ObjectProperty<UserRole> currentRoleProperty() {
    return currentRole;
  }

  public SimpleDoubleProperty currentBalanceProperty() {
    return currentBalance;
  }

  public SimpleStringProperty currentNameProperty() {
    return currentName;
  }

  public SimpleStringProperty phoneNumberProperty() {
    return phoneNumber;
  }

  public SimpleStringProperty emailProperty() {
    return email;
  }

  public SimpleStringProperty avatarUrlProperty() {
    return avatarUrl;
  }

  public SimpleIntegerProperty itemsSoldProperty() {
    return itemsSold;
  }

  public SimpleDoubleProperty averageRatingProperty() {
    return averageRating;
  }

  public SimpleDoubleProperty moneyReceivedProperty() {
    return moneyReceived;
  }

  public SimpleIntegerProperty itemsBoughtProperty() {
    return itemsBought;
  }

  public SimpleDoubleProperty moneySpentProperty() {
    return moneySpent;
  }

  public void clear() {
    currentUser.setValue(null);
    currentRole.setValue(null);
    currentBalance.setValue(0.0);
    currentName.setValue(null);
    phoneNumber.setValue(null);
    email.setValue(null);
    avatarUrl.setValue(null);
  }
}
