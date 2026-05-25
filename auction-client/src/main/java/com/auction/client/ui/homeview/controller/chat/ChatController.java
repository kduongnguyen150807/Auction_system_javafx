package com.auction.client.ui.homeview.controller.chat;

import com.auction.client.app.AutoInject;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.userinformation.SelectedUser;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.util.FXThread;
import javafx.fxml.FXML;

public class ChatController implements CanRefresh {
  @FXML private SideBarController sideBarController;
  @FXML private MainContainerController mainContainerController;

  private final ClientService clientService;

  @AutoInject
  public ChatController(ClientService clientService) {
    this.clientService = clientService;
  }

  @FXML
  private void initialize() {
    clientService.getFriendsList();
    clientService.getFriendRequest();
    sideBarController.bindList(
      ClientSession.CURRENT_SESSION.getFriendsList().getIdSet(),
      ClientSession.CURRENT_SESSION.getRequestList().getIdSet()
    );

    SelectedUser.SELECTED_USER.selectedUserProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        clientService.getChatHistory(newValue.getUser().getId())
          .thenAccept(v -> {
            FXThread.run(() -> {
              mainContainerController.setFilteredChatMessages(
                newValue.getUser().getUsername(),
                SelectedUser.SELECTED_USER.getSelectedChatHistory()
              );
            });
          });
      } else {
        clientService.getGlobalChatHistory()
          .thenAccept(v -> {
            FXThread.run(() -> {
              mainContainerController.setFilteredChatMessages(
                MainContainerController.GLOBAL_CHAT,
                SelectedUser.SELECTED_USER.getSelectedChatHistory()
              );
            });
          });
      }
    });

    clientService.getGlobalChatHistory()
      .thenAccept(v -> {
        FXThread.run(() -> {
          mainContainerController.setFilteredChatMessages(
            MainContainerController.GLOBAL_CHAT,
            SelectedUser.SELECTED_USER.getSelectedChatHistory()
          );
        });
      });
  }

  @Override
  public void refreshData() {
    clientService.getFriendsList();
    clientService.getFriendRequest();
  }
}
