package com.auction.client.ui.homeview.controller.chat;

import com.auction.client.app.AutoInject;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.userinformation.SelectedUser;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.component.ChatBubble;
import com.auction.client.ui.homeview.homeviewcomponent.VBoxModel;
import com.auction.shared.ChatMessage;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class MainContainerController implements CanRefresh {
  public static String GLOBAL_CHAT = "GLOBAL_CHAT";

  @FXML private VBoxModel<ChatMessage> messagesContainer;
  @FXML private Label chatTitle;
  @FXML private HBox inputBar;
  @FXML private TextField messageInput;
  @FXML private Button handleSend;

  boolean isGlobalChat = false;

  private ClientService clientService;

  @AutoInject
  public MainContainerController(ClientService clientService) {
    this.clientService = clientService;
  }

  private FilteredList<ChatMessage> filteredChatMessages;

  public void setFilteredChatMessages(String chatName, ObservableList<ChatMessage> filteredChatMessages) {
    if (chatName.equals(GLOBAL_CHAT)) {
      isGlobalChat = true;
    } else {
      isGlobalChat = false;
    }

    this.filteredChatMessages = new FilteredList<>(filteredChatMessages);
    chatTitle.setText(chatName);
    applyChatHistory(filteredChatMessages);
    inputBar.setVisible(true);
    inputBar.setManaged(true);
  }

  private void applyChatHistory(ObservableList<ChatMessage> chatMessages) {
    messagesContainer.bind(
      chatMessages,
      param -> {
        ChatBubble chatBubble = new ChatBubble(param);
        boolean isMine = param.getSenderId() == ClientSession.CURRENT_SESSION.getUser().getId();
        chatBubble.setPlace(isMine, param);
        return chatBubble;
      });
  }

  @FXML
  private void handleSend() {
    String message = messageInput.getText();
    if (message == null || message.trim().isEmpty()) {
      return;
    }

    ChatMessage msg = new ChatMessage();
    msg.setContent(message);
    msg.setSenderId(ClientSession.CURRENT_SESSION.getUser().getId());
    msg.setSenderUsername(ClientSession.CURRENT_SESSION.getUser().getUsername());
    if (isGlobalChat) {
      msg.setMessageType(ChatMessage.TYPE_GLOBAL);
    } else if (!isGlobalChat && SelectedUser.SELECTED_USER.getSelectedUser().getUser().getId() != 0) {
      msg.setMessageType(ChatMessage.TYPE_PRIVATE);
      msg.setReceiverId(SelectedUser.SELECTED_USER.getSelectedUser().getUser().getId());
    } else {
      return;
    }
    clientService.sendMessage(msg);
    messageInput.clear();
  }

  @Override
  public void refreshData() {

  }
}
