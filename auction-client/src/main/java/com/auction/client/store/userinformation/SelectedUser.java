package com.auction.client.store.userinformation;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.shared.Item;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class SelectedUser {
  public static SelectedUser SELECTED_USER = new SelectedUser();

  private ObjectProperty<UserModel> selectedUser = new SimpleObjectProperty<>();

  private List<ItemModel> selectedUserItems = new ArrayList<>();

  private SelectedUser () {}

  public void setSelectedUser(UserModel user) {
    selectedUser.setValue(user);
  }

  public UserModel getSelectedUser() {
    return selectedUser.get();
  }

  public ObjectProperty<UserModel> selectedUserProperty() {
    return selectedUser;
  }

  public void setSelectedUserItems(List<Item> items) {
    selectedUserItems.clear();
    for (Item item : items) {
      selectedUserItems.add(new ItemModel(item));
    }
  }

  public List<ItemModel> getSelectedUserItems() {
    return selectedUserItems;
  }
}
