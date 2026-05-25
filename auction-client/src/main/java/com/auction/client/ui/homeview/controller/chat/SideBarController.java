package com.auction.client.ui.homeview.controller.chat;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.userinformation.SelectedUser;
import com.auction.client.store.userinformation.UserModel;
import com.auction.client.ui.component.userbar.UserBar;
import com.auction.client.ui.component.userbar.UserBarConfig;
import com.auction.client.ui.component.userbar.UserBarMode;
import com.auction.client.ui.homeview.homeviewcomponent.VBoxModel;
import com.auction.client.util.FXThread;
import com.auction.shared.Friendship;
import com.auction.shared.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SideBarController {
  @FXML private VBoxModel<UserModel> leftList;
  @FXML private TextField searchField;
  @FXML private Button requestsTabBtn;
  @FXML private Button friendsTabBtn;

  private final AuctionDiscoveryService discoveryService;
  private final ClientService clientService;

  private final ObservableList<UserModel> friendsList = FXCollections.observableArrayList();
  private final ObservableList<UserModel> requestsList = FXCollections.observableArrayList();

  private FilteredList<UserModel> bindList;

  Consumer<UserModel> onUserClicked = (userModel) -> {
    SelectedUser.SELECTED_USER.setSelectedUser(userModel);
  };

  @AutoInject
  public SideBarController(AuctionDiscoveryService discoveryService, ClientService clientService) {
    this.discoveryService = discoveryService;
    this.clientService = clientService;
  }

  @FXML
  public void initialize() {
    bindList = new FilteredList<>(friendsList, user -> true);

    leftList.bind(
      bindList,
      param -> {
        UserBar userBar = new UserBar(param);
        userBar.setOnUserBarClicked(onUserClicked);
        userBar.setDisplayMode(UserBarMode.FRIEND);
        return userBar;
      });
  }

  public void bindList(ObservableSet<Friendship> friendships, ObservableSet<Friendship> requestsList) {
    friendships.addListener((SetChangeListener<? super Friendship>) change -> {
      applyFriendList(friendships);
    });
    applyFriendList(friendships);

    requestsList.addListener((SetChangeListener<? super Friendship>) change -> {
      applyRequestsList(requestsList);
    });
    applyRequestsList(requestsList);
  }

  private void applyFriendList(ObservableSet<Friendship> friendships) {
    int myUserId = ClientSession.CURRENT_SESSION.getUser().getId();

    List<UserModel> updatedUsers = new ArrayList<>();

    friendships.forEach(friendship -> {
      System.out.println(friendship.toString());
      updatedUsers.add(UserModel.toUserModel(friendship, myUserId));
    });

    friendsList.setAll(updatedUsers);
  }

  private void applyRequestsList(ObservableSet<Friendship> newRequestsList) {
    int myUserId = ClientSession.CURRENT_SESSION.getUser().getId();

    List<UserModel> updatedUsers = new ArrayList<>();

    newRequestsList.forEach(friendship -> {
      System.out.println(friendship.toString());
      updatedUsers.add(UserModel.toUserModel(friendship, myUserId));
    });

    this.requestsList.setAll(updatedUsers);
  }

  @FXML
  private void switchToFriends() {
    friendsTabBtn.getStyleClass().add("chat-tab-active");
    requestsTabBtn.getStyleClass().remove("chat-tab-active");
    loadFriendsList();
  }

  @FXML
  private void switchToRequests() {
    requestsTabBtn.getStyleClass().add("chat-tab-active");
    friendsTabBtn.getStyleClass().remove("chat-tab-active");
    loadRequestsList();
  }

  @FXML
  private void handleSearch() {
    String keyWord = searchField.getText();
    if (keyWord == null || keyWord.trim().isEmpty()) {
      loadFriendsList();
      return;
    }

    discoveryService.getUsers(keyWord.toLowerCase())
      .thenAccept(users -> {
        List<UserModel> searchResultModels = new ArrayList<>();
        for (User user : users) {
          searchResultModels.add(new UserModel(user));
        }
        FXThread.run(() -> {
          leftList.setAll(
            searchResultModels,
            param -> {
              UserBar userBar = new UserBar(param);
              userBar.setOnUserBarClicked(onUserClicked);

              int targetId = param.getUser().getId();
              if (targetId == ClientSession.CURRENT_SESSION.getUser().getId()) {
                return null;
              }
              if (checkIsFriend(targetId)) {
                userBar.setDisplayMode(UserBarMode.FRIEND);
              } else {
                userBar.setDisplayMode(UserBarMode.STRANGER);
                userBar.setOnAddFriend(() -> {
                  discoveryService.sendFriendRequest(targetId);
                });
              }
              return userBar;
            });
        });
      })
      .exceptionally(ex -> {
        ex.printStackTrace();
        return null;
      });
  }

  @FXML
  private void openGlobalChat() {
    SelectedUser.SELECTED_USER.setSelectedUser(null);
  }

  private void loadFriendsList() {
    bindList = new FilteredList<>(friendsList, user -> true);
    leftList.bind(
      bindList,
      param -> {
        UserBar userBar = new UserBar(param);
        userBar.setOnUserBarClicked(onUserClicked);
        userBar.setDisplayMode(UserBarMode.FRIEND);
        return userBar;
      });
  }

  private void loadRequestsList() {
    bindList = new FilteredList<>(requestsList, user -> true);
    leftList.bind(
      bindList,
      param -> {
        UserBar userBar = new UserBar(param);
        userBar.setDisplayMode(UserBarMode.REQUEST);
        userBar.setOnUserBarClicked(onUserClicked);
        userBar.setOnAccept(() -> {
          clientService.acceptFriendRequest(param.getUser().getId());
        });
        userBar.setOnDecline(() -> {
          clientService.declineFriendRequest(param.getUser().getId());
        });
        return userBar;
      });
  }

  private boolean checkIsFriend(int targetUserId) {
    return friendsList.stream()
      .anyMatch(friendModel -> friendModel.getUser().getId() == targetUserId);
  }
}
