package com.auction.client.ui.Chat;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.shared.*;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ChatPageController {
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  @FXML private Button friendsTabBtn;
  @FXML private Button requestsTabBtn;
  @FXML private TextField searchField;
  @FXML private ListView<Object> leftList;
  @FXML private Button globalChatBtn;
  @FXML private HBox chatHeader;
  @FXML private Label chatTitle;
  @FXML private Label chatSubtitle;
  @FXML private ScrollPane messagesScroll;
  @FXML private VBox messagesContainer;
  @FXML private HBox inputBar;
  @FXML private TextField messageInput;

  private enum LeftTab { FRIENDS, REQUESTS, SEARCH }
  private enum ChatMode { NONE, GLOBAL, PRIVATE }

  private LeftTab currentTab = LeftTab.FRIENDS;
  private ChatMode chatMode = ChatMode.NONE;
  private int dmPartnerId = -1;
  private String dmPartnerName = "";

  @FXML
  public void initialize() {
    leftList.setCellFactory(lv -> new LeftListCell());
    leftList.setOnMouseClicked(e -> handleLeftListClick());
    loadFriends();
  }

  // ─── Left tabs ─────────────────────────────────────────────────

  @FXML
  private void switchToFriends() {
    currentTab = LeftTab.FRIENDS;
    friendsTabBtn.getStyleClass().add("chat-tab-active");
    requestsTabBtn.getStyleClass().remove("chat-tab-active");
    loadFriends();
  }

  @FXML
  private void switchToRequests() {
    currentTab = LeftTab.REQUESTS;
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
    currentTab = LeftTab.SEARCH;
    friendsTabBtn.getStyleClass().remove("chat-tab-active");
    requestsTabBtn.getStyleClass().remove("chat-tab-active");
    asyncRequest(new Request(Request.SEARCH_USERS, kw.trim()), res -> {
      if (res.getPayload() instanceof List<?> list) {
        Platform.runLater(() -> {
          ObservableList<Object> items = FXCollections.observableArrayList();
          for (Object o : list) {
            if (o instanceof User u && u.getId() != myId()) items.add(u);
          }
          leftList.setItems(items);
        });
      }
    });
  }

  // ─── Global chat ───────────────────────────────────────────────

  @FXML
  private void openGlobalChat() {
    chatMode = ChatMode.GLOBAL;
    dmPartnerId = -1;
    chatTitle.setText("Global Chat");
    chatSubtitle.setText("Everyone can see");
    inputBar.setVisible(true);
    inputBar.setManaged(true);
    messagesContainer.getChildren().clear();
    asyncRequest(new Request(Request.GET_GLOBAL_CHAT_HISTORY, null), res -> {
      if (res.getPayload() instanceof List<?> list) {
        Platform.runLater(() -> {
          for (Object o : list) {
            if (o instanceof ChatMessage m) appendBubble(m);
          }
          scrollBottom();
        });
      }
    });
  }

  // ─── Send message ──────────────────────────────────────────────

  @FXML
  private void handleSend() {
    String text = messageInput.getText();
    if (text == null || text.trim().isEmpty()) return;
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
    asyncRequest(new Request(Request.SEND_CHAT, msg), res -> {});
  }

  // ─── Left list click ──────────────────────────────────────────

  private void handleLeftListClick() {
    Object selected = leftList.getSelectionModel().getSelectedItem();
    if (selected == null) return;

    if (currentTab == LeftTab.FRIENDS && selected instanceof Friendship f) {
      int partnerId = f.getRequesterId() == myId() ? f.getAddresseeId() : f.getRequesterId();
      String partnerName = f.getRequesterId() == myId() ? f.getAddresseeUsername() : f.getRequesterUsername();
      openPrivateChat(partnerId, partnerName);
    } else if (currentTab == LeftTab.SEARCH && selected instanceof User u) {
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
    asyncRequest(new Request(Request.GET_PRIVATE_CHAT_HISTORY, (Serializable) data), res -> {
      if (res.getPayload() instanceof List<?> list) {
        Platform.runLater(() -> {
          for (Object o : list) {
            if (o instanceof ChatMessage m) appendBubble(m);
          }
          scrollBottom();
        });
      }
    });
  }

  // ─── Data loading ──────────────────────────────────────────────

  private void loadFriends() {
    asyncRequest(new Request(Request.GET_FRIENDS, myId()), res -> {
      if (res.getPayload() instanceof List<?> list) {
        Platform.runLater(() -> {
          ObservableList<Object> items = FXCollections.observableArrayList();
          for (Object o : list) {
            if (o instanceof Friendship) items.add(o);
          }
          leftList.setItems(items);
        });
      }
    });
  }

  private void loadFriendRequests() {
    asyncRequest(new Request(Request.GET_FRIEND_REQUESTS, myId()), res -> {
      if (res.getPayload() instanceof List<?> list) {
        Platform.runLater(() -> {
          ObservableList<Object> items = FXCollections.observableArrayList();
          for (Object o : list) {
            if (o instanceof Friendship) items.add(o);
          }
          leftList.setItems(items);
        });
      }
    });
  }

  // ─── Friend actions ────────────────────────────────────────────

  private void addFriend(int targetId) {
    asyncRequest(new Request(Request.ADD_FRIEND, targetId), res -> {});
  }

  private void acceptFriend(int requesterId) {
    asyncRequest(new Request(Request.ACCEPT_FRIEND, requesterId), res -> {});
  }

  private void declineFriend(int requesterId) {
    asyncRequest(new Request(Request.DECLINE_FRIEND, requesterId), res -> {
      Platform.runLater(() -> {
        if (currentTab == LeftTab.REQUESTS) loadFriendRequests();
      });
    });
  }

  // ─── Delegated events (called by KhungController) ──────────────

  public void onGlobalChat(ChatMessage message) {
    if (chatMode == ChatMode.GLOBAL) {
      appendBubble(message);
      scrollBottom();
    }
  }

  public void onPrivateChat(ChatMessage message) {
    if (chatMode == ChatMode.PRIVATE && dmPartnerId > 0) {
      if (message.getSenderId() == dmPartnerId || message.getReceiverId() == dmPartnerId) {
        appendBubble(message);
        scrollBottom();
      }
    }
  }

  public void onFriendRequest(Friendship friendship) {
    if (currentTab == LeftTab.REQUESTS) loadFriendRequests();
  }

  public void onFriendRequestSent(Friendship friendship) {
    if (currentTab == LeftTab.SEARCH) handleSearch();
  }

  public void onFriendAccepted(Friendship friendship) {
    if (currentTab == LeftTab.FRIENDS) loadFriends();
    if (currentTab == LeftTab.REQUESTS) loadFriendRequests();
  }

  // ─── UI rendering ─────────────────────────────────────────────

  private void appendBubble(ChatMessage msg) {
    boolean mine = msg.getSenderId() == myId();
    Label sender = new Label(msg.getSenderUsername() != null ? msg.getSenderUsername() : "User");
    sender.getStyleClass().add("msg-sender");

    Label text = new Label(msg.getContent());
    text.getStyleClass().add("msg-text");
    text.setWrapText(true);
    text.setMaxWidth(350);

    String timeStr = msg.getCreatedAt() != null ? msg.getCreatedAt().format(TIME_FMT) : "";
    Label time = new Label(timeStr);
    time.getStyleClass().add("msg-time");

    VBox bubble = new VBox(2, sender, text, time);
    bubble.getStyleClass().add(mine ? "msg-bubble-mine" : "msg-bubble");
    bubble.setMaxWidth(380);

    HBox row = new HBox(bubble);
    row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
    row.setMaxWidth(Double.MAX_VALUE);

    messagesContainer.getChildren().add(row);
  }

  private void scrollBottom() {
    Platform.runLater(() -> messagesScroll.setVvalue(1.0));
  }


  // ─── Cell factory ─────────────────────────────────────────────

  private class LeftListCell extends ListCell<Object> {
    @Override
    protected void updateItem(Object item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setGraphic(null);
        return;
      }

      if (currentTab == LeftTab.FRIENDS && item instanceof Friendship f) {
        boolean iAmRequester = f.getRequesterId() == myId();
        String name = iAmRequester ? f.getAddresseeUsername() : f.getRequesterUsername();
        setText(null);
        Label nameLabel = new Label(name != null ? name : "User");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600;");
        HBox row = new HBox(10, nameLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        setGraphic(row);

      } else if (currentTab == LeftTab.REQUESTS && item instanceof Friendship f) {
        setText(null);
        Label nameLabel = new Label(f.getRequesterUsername() != null ? f.getRequesterUsername() : "User");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        Button acceptBtn = new Button("Accept");
        acceptBtn.getStyleClass().add("accept-btn");
        acceptBtn.setOnAction(e -> acceptFriend(f.getRequesterId()));
        Button declineBtn = new Button("Decline");
        declineBtn.getStyleClass().add("decline-btn");
        declineBtn.setOnAction(e -> declineFriend(f.getRequesterId()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, nameLabel, spacer, acceptBtn, declineBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 4, 2, 0));
        setGraphic(row);

      } else if (currentTab == LeftTab.SEARCH && item instanceof User u) {
        setText(null);
        Label nameLabel = new Label(u.getUsername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        String fullName = u.getFullName();
        Label subLabel = new Label(fullName != null && !fullName.isBlank() ? fullName : "");
        subLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");
        VBox info = new VBox(1, nameLabel, subLabel);
        Button addBtn = new Button("+ Add");
        addBtn.getStyleClass().add("add-friend-btn");
        addBtn.setOnAction(e -> addFriend(u.getId()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, info, spacer, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 4, 2, 0));
        setGraphic(row);

      } else {
        setText(item.toString());
        setGraphic(null);
      }
    }
  }

  // ─── Helpers ───────────────────────────────────────────────────

  private int myId() {
    return ClientSession.getCurrentUser() != null ? ClientSession.getCurrentUser().getId() : 0;
  }

  private String myName() {
    return ClientSession.getCurrentUser() != null ? ClientSession.getCurrentUser().getUsername() : "";
  }

  private void asyncRequest(Request req, java.util.function.Consumer<Response> callback) {
    Thread t = new Thread(() -> {
      Response res = NetworkClient.getInstance().sendRequestAndWait(req);
      if (res != null) callback.accept(res);
    });
    t.setDaemon(true);
    t.start();
  }
}
