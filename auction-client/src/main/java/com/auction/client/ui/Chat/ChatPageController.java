package com.auction.client.ui.Chat;

import com.auction.client.ClientSession;
import com.auction.shared.ChatMessage;
import com.auction.shared.Friendship;
import com.auction.shared.Request;
import com.auction.shared.User;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatPageController implements ChatLeftListHost {
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  @FXML private Button friendsTabBtn, requestsTabBtn;
  @FXML private TextField searchField;
  @FXML private ListView<Object> leftList;
  @FXML private Button globalChatBtn;
  @FXML private HBox chatHeader, inputBar;
  @FXML private Label chatTitle, chatSubtitle;
  @FXML private ScrollPane messagesScroll;
  @FXML private VBox messagesContainer;
  @FXML private TextField messageInput;

  private enum ChatMode {
    NONE,
    GLOBAL,
    PRIVATE
  }

  private ChatSidebarTab currentTab = ChatSidebarTab.FRIENDS;
  private ChatMode chatMode = ChatMode.NONE;
  private int dmPartnerId = -1;
  private String dmPartnerName = "";

  @FXML
  public void initialize() {
    leftList.setCellFactory(lv -> new ChatLeftListCell(this));
    leftList.setOnMouseClicked(e -> handleLeftListClick());
    loadFriends();
  }

  @FXML
  private void switchToFriends() {
    currentTab = ChatSidebarTab.FRIENDS;
    friendsTabBtn.getStyleClass().add("chat-tab-active");
    requestsTabBtn.getStyleClass().remove("chat-tab-active");
    loadFriends();
  }

  @FXML
  private void switchToRequests() {
    currentTab = ChatSidebarTab.REQUESTS;
    requestsTabBtn.getStyleClass().add("chat-tab-active");
    friendsTabBtn.getStyleClass().remove("chat-tab-active");
    loadFriendRequests();
  }

  @FXML
  private void handleSearch() {
    String kw = searchField.getText();
    if (kw == null || kw.trim().isEmpty()) {
      switchToFriends();
      return;
    }
    currentTab = ChatSidebarTab.SEARCH;
    friendsTabBtn.getStyleClass().remove("chat-tab-active");
    requestsTabBtn.getStyleClass().remove("chat-tab-active");
    ChatAsyncRequests.submit(
        new Request(Request.SEARCH_USERS, kw.trim()),
        res -> {
          if (res.getPayload() instanceof List<?> list) {
            Platform.runLater(
                () -> {
                  ObservableList<Object> items = FXCollections.observableArrayList();
                  for (Object o : list) {
                    if (o instanceof User u && u.getId() != myId()) {
                      items.add(u);
                    }
                  }
                  leftList.setItems(items);
                });
          }
        });
  }

  @FXML
  private void openGlobalChat() {
    chatMode = ChatMode.GLOBAL;
    dmPartnerId = -1;
    chatTitle.setText("Global Chat");
    chatSubtitle.setText("Everyone can see");
    inputBar.setVisible(true);
    inputBar.setManaged(true);
    messagesContainer.getChildren().clear();
    ChatAsyncRequests.submit(
        new Request(Request.GET_GLOBAL_CHAT_HISTORY, null),
        res -> {
          if (res.getPayload() instanceof List<?> list) {
            Platform.runLater(
                () -> {
                  for (Object o : list) {
                    if (o instanceof ChatMessage m) {
                      appendBubble(m);
                    }
                  }
                  scrollBottom();
                });
          }
        });
  }

  @FXML
  private void handleSend() {
    String text = messageInput.getText();
    if (text == null || text.trim().isEmpty()) {
      return;
    }
    messageInput.clear();
    ChatMessage msg = new ChatMessage();
    msg.setContent(text.trim());
    msg.setSenderId(myId());
    msg.setSenderUsername(myName());
    if (chatMode == ChatMode.GLOBAL) {
      msg.setMessageType(ChatMessage.TYPE_GLOBAL);
    } else if (chatMode == ChatMode.PRIVATE && dmPartnerId > 0) {
      msg.setMessageType(ChatMessage.TYPE_PRIVATE);
      msg.setReceiverId(dmPartnerId);
    } else {
      return;
    }
    ChatAsyncRequests.submit(new Request(Request.SEND_CHAT, msg), res -> {});
  }

  private void handleLeftListClick() {
    Object selected = leftList.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    if (currentTab == ChatSidebarTab.FRIENDS && selected instanceof Friendship f) {
      int partnerId = f.getRequesterId() == myId() ? f.getAddresseeId() : f.getRequesterId();
      String partnerName =
          f.getRequesterId() == myId() ? f.getAddresseeUsername() : f.getRequesterUsername();
      openPrivateChat(partnerId, partnerName);
    } else if (currentTab == ChatSidebarTab.SEARCH && selected instanceof User u) {
      openPrivateChat(u.getId(), u.getUsername());
    }
  }

  private void openPrivateChat(int partnerId, String partnerName) {
    chatMode = ChatMode.PRIVATE;
    dmPartnerId = partnerId;
    dmPartnerName = partnerName;
    chatTitle.setText(partnerName);
    chatSubtitle.setText("Private conversation");
    inputBar.setVisible(true);
    inputBar.setManaged(true);
    messagesContainer.getChildren().clear();
    Map<String, Object> data = new HashMap<>();
    data.put("myId", myId());
    data.put("otherId", partnerId);
    ChatAsyncRequests.submit(
        new Request(Request.GET_PRIVATE_CHAT_HISTORY, (Serializable) data),
        res -> {
          if (res.getPayload() instanceof List<?> list) {
            Platform.runLater(
                () -> {
                  for (Object o : list) {
                    if (o instanceof ChatMessage m) {
                      appendBubble(m);
                    }
                  }
                  scrollBottom();
                });
          }
        });
  }

  private void loadFriends() {
    ChatAsyncRequests.submit(
        new Request(Request.GET_FRIENDS, myId()),
        res -> {
          if (res.getPayload() instanceof List<?> list) {
            Platform.runLater(
                () -> {
                  ObservableList<Object> items = FXCollections.observableArrayList();
                  for (Object o : list) {
                    if (o instanceof Friendship) {
                      items.add(o);
                    }
                  }
                  leftList.setItems(items);
                });
          }
        });
  }

  private void loadFriendRequests() {
    ChatAsyncRequests.submit(
        new Request(Request.GET_FRIEND_REQUESTS, myId()),
        res -> {
          if (res.getPayload() instanceof List<?> list) {
            Platform.runLater(
                () -> {
                  ObservableList<Object> items = FXCollections.observableArrayList();
                  for (Object o : list) {
                    if (o instanceof Friendship) {
                      items.add(o);
                    }
                  }
                  leftList.setItems(items);
                });
          }
        });
  }

  /** Reload friends / requests / search list when user opens Chat from the sidebar. */
  public void refreshOnNavigate() {
    switch (currentTab) {
      case FRIENDS -> loadFriends();
      case REQUESTS -> loadFriendRequests();
      case SEARCH -> handleSearch();
    }
  }

  @Override
  public ChatSidebarTab currentSidebarTab() {
    return currentTab;
  }

  @Override
  public int currentUserId() {
    return myId();
  }

  @Override
  public void acceptFriend(int requesterId) {
    ChatAsyncRequests.submit(new Request(Request.ACCEPT_FRIEND, requesterId), res -> {});
  }

  @Override
  public void declineFriend(int requesterId) {
    ChatAsyncRequests.submit(
        new Request(Request.DECLINE_FRIEND, requesterId),
        res ->
            Platform.runLater(
                () -> {
                  if (currentTab == ChatSidebarTab.REQUESTS) {
                    loadFriendRequests();
                  }
                }));
  }

  @Override
  public void addFriend(int userId) {
    ChatAsyncRequests.submit(new Request(Request.ADD_FRIEND, userId), res -> {});
  }

  public void onGlobalChat(ChatMessage m) {
    if (chatMode == ChatMode.GLOBAL) {
      appendBubble(m);
      scrollBottom();
    }
  }

  public void onPrivateChat(ChatMessage m) {
    if (chatMode == ChatMode.PRIVATE
        && dmPartnerId > 0
        && (m.getSenderId() == dmPartnerId || m.getReceiverId() == dmPartnerId)) {
      appendBubble(m);
      scrollBottom();
    }
  }

  public void onFriendRequest(Friendship f) {
    if (currentTab == ChatSidebarTab.REQUESTS) {
      loadFriendRequests();
    }
  }

  public void onFriendRequestSent(Friendship f) {
    if (currentTab == ChatSidebarTab.SEARCH) {
      handleSearch();
    }
  }

  public void onFriendAccepted(Friendship f) {
    if (currentTab == ChatSidebarTab.FRIENDS) {
      loadFriends();
    }
    if (currentTab == ChatSidebarTab.REQUESTS) {
      loadFriendRequests();
    }
  }

  private void appendBubble(ChatMessage msg) {
    boolean mine = msg.getSenderId() == myId();
    messagesContainer.getChildren().add(ChatBubbleRowFactory.createRow(msg, mine, TIME_FMT));
  }

  private void scrollBottom() {
    Platform.runLater(() -> messagesScroll.setVvalue(1.0));
  }

  private int myId() {
    return ClientSession.getCurrentUser() != null ? ClientSession.getCurrentUser().getId() : 0;
  }

  private String myName() {
    return ClientSession.getCurrentUser() != null ? ClientSession.getCurrentUser().getUsername() : "";
  }
}
