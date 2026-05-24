package com.auction.client.store.clientinformation;

import com.auction.client.store.userinformation.UserModel;
import com.auction.shared.User;
import com.auction.shared.UserRole;
import javafx.beans.property.*;

public class ClientSession extends UserModel {
  public static final ClientSession CURRENT_SESSION = new ClientSession();

  private IdStateManager watchedItemsList = new IdStateManager();

  private final SimpleIntegerProperty itemsSold = new SimpleIntegerProperty();
  private final SimpleDoubleProperty moneyReceived = new SimpleDoubleProperty();
  private final SimpleIntegerProperty itemsBought = new SimpleIntegerProperty();
  private final SimpleDoubleProperty averageRating = new SimpleDoubleProperty();

  private ClientSession() {
    super();
  }

  public void setUser(User user) {
    super.updateUser(user);

    if (user != null) {
      this.itemsSold.setValue(user.getItemsSold());
      this.moneyReceived.setValue(user.getMoneyReceived());
      this.itemsBought.setValue(user.getItemsBought());
      this.averageRating.setValue(user.getAvgRating());
    }
  }

  public void applyProfileUpdate(String fullname, String phoneNumber, String email) {
    this.fullName.setValue(fullname);
    this.phoneNumber.setValue(phoneNumber);
    this.email.setValue(email);
    if (this.user != null) {
      this.user.setFullName(fullname);
      this.user.setPhoneNumber(phoneNumber);
      this.user.setEmail(email);
    }
  }

  public void applyAvatarUrl(String avatarUrl) {
    this.avatarUrl.setValue(avatarUrl);
    if (this.user != null) {
      this.user.setAvatarUrl(avatarUrl);
    }
  }

  public void deposit(double newBalance) {
    this.currentBalance.setValue(this.currentBalance.getValue() + newBalance);
    if (this.user != null) {
      this.user.setBalance(this.currentBalance.get());
    }
  }

  public SimpleIntegerProperty itemsSoldProperty() { return itemsSold; }
  public SimpleDoubleProperty moneyReceivedProperty() { return moneyReceived; }
  public SimpleIntegerProperty itemsBoughtProperty() { return itemsBought; }
  public SimpleDoubleProperty averageRatingProperty() { return averageRating; }

  public IdStateManager getWatchedItemsList() { return watchedItemsList; }
  public User getCurrentUser() { return getUser(); }

  public ObjectProperty<UserRole> currentRoleProperty() { return roleProperty(); }
  public SimpleStringProperty currentNameProperty() { return (SimpleStringProperty) fullNameProperty(); }

  public void clear() {
    watchedItemsList = new IdStateManager();
    super.clearModel();

    itemsSold.setValue(0);
    moneyReceived.setValue(0.0);
    itemsBought.setValue(0);
    averageRating.setValue(0.0);
  }
}