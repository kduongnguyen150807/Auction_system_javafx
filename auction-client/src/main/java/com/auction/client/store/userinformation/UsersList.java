package com.auction.client.store.userinformation;

import com.auction.client.util.FXThread;
import com.auction.shared.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersList {
  public static UsersList USER_LIST = new UsersList();

  private UsersList() {}

  private final ObservableList<UserModel> users = FXCollections.observableArrayList();

  private final Map<Integer, UserModel> userModelMap = new HashMap<>();

  public ObservableList<UserModel> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    FXThread.run(() -> {
      this.users.clear();
      userModelMap.clear();
      for (User user : users) {
        UserModel userModel = new UserModel(user);
        this.users.add(userModel);
        userModelMap.put(user.getId(), userModel);
      }
    });
  }

  public void updateUser(User user) {
    FXThread.run(() -> {
      if (userModelMap.containsKey(user.getId())) {
        UserModel userModel = userModelMap.get(user.getId());
        userModel.updateUser(user);
      }
    });
  }
}
