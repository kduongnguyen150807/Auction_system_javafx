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

  @FXML private Button friendsTabBtn, requestsTabBtn;
  @FXML private TextField searchField;
  @FXML private ListView<Object> leftList;
  @FXML private Button globalChatBtn;
  @FXML private HBox chatHeader, inputBar;
  @FXML private Label chatTitle, chatSubtitle;
  @FXML private ScrollPane messagesScroll;
  @FXML private VBox messagesContainer;
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
    if (kw == null || kw.trim().isEmpty()) { switchToFriends(); return; }
    currentTab = LeftTab.SEARCH;
    friendsTabBtn.getStyleClass().remove("chat-tab-active");
    requestsTabBtn.getStyleClass().remove("chat-tab-active");
    async(new Request(Request.SEARCH_USERS, kw.trim()), res -> {
      if (res.getPayload() instanceof List<?> list) {
        Platform.runLater(() -> {
          ObservableList<Object> items = FXCollections.observableArrayList();
          for (Object o : list) if (o instanceof User u && u.getId() != myId()) items.add(u);
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
    inputBar.setVisible(true); inputBar.setManaged(true);
    messagesContainer.getChildren().clear();
    async(new Request(Request.GET_GLOBAL_CHAT_HISTORY, null), res -> {
      if (res.getPayload() instanceof List<?> list)
        Platform.runLater(() -> { for (Object o : list) if (o instanceof ChatMessage m) appendBubble(m); scrollBottom(); });
    });
  }

  @FXML
  private void handleSend() {
    String text = messageInput.getText();
    if (text == null || text.trim().isEmpty()) return;
    messageInput.clear();
    ChatMessage msg = new ChatMessage();
    msg.setContent(text.trim());
    msg.setSenderId(myId());
    msg.setSenderUsername(myName());
    if (chatMode == ChatMode.GLOBAL) msg.setMessageType(ChatMessage.TYPE_GLOBAL);
    else if (chatMode == ChatMode.PRIVATE && dmPartnerId > 0) {
      msg.setMessageType(ChatMessage.TYPE_PRIVATE);
      msg.setReceiverId(dmPartnerId);
    } else return;
    async(new Request(Request.SEND_CHAT, msg), res -> {});
  }

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
    inputBar.setVisible(true); inputBar.setManaged(true);
    messagesContainer.getChildren().clear();
    Map<String, Object> data = new HashMap<>();
    data.put("myId", myId()); data.put("otherId", partnerId);
    async(new Request(Request.GET_PRIVATE_CHAT_HISTORY, (Serializable) data), res -> {
      if (res.getPayload() instanceof List<?> list)
        Platform.runLater(() -> { for (Object o : list) if (o instanceof ChatMessage m) appendBubble(m); scrollBottom(); });
    });
  }

  private void loadFriends() {
    async(new Request(Request.GET_FRIENDS, myId()), res -> {
      if (res.getPayload() instanceof List<?> list) Platform.runLater(() -> {
        ObservableList<Object> items = FXCollections.observableArrayList();
        for (Object o : list) if (o instanceof Friendship) items.add(o);
        leftList.setItems(items);
      });
    });
  }

  private void loadFriendRequests() {
    async(new Request(Request.GET_FRIEND_REQUESTS, myId()), res -> {
      if (res.getPayload() instanceof List<?> list) Platform.runLater(() -> {
        ObservableList<Object> items = FXCollections.observableArrayList();
        for (Object o : list) if (o instanceof Friendship) items.add(o);
        leftList.setItems(items);
      });
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

  private void addFriend(int targetId) { async(new Request(Request.ADD_FRIEND, targetId), res -> {}); }
  private void acceptFriend(int id) { async(new Request(Request.ACCEPT_FRIEND, id), res -> {}); }
  private void declineFriend(int id) {
    async(new Request(Request.DECLINE_FRIEND, id), res ->
        Platform.runLater(() -> { if (currentTab == LeftTab.REQUESTS) loadFriendRequests(); }));
  }

  public void onGlobalChat(ChatMessage m) { if (chatMode == ChatMode.GLOBAL) { appendBubble(m); scrollBottom(); } }
  public void onPrivateChat(ChatMessage m) {
    if (chatMode == ChatMode.PRIVATE && dmPartnerId > 0
        && (m.getSenderId() == dmPartnerId || m.getReceiverId() == dmPartnerId)) {
      appendBubble(m); scrollBottom();
    }
  }
  public void onFriendRequest(Friendship f) { if (currentTab == LeftTab.REQUESTS) loadFriendRequests(); }
  public void onFriendRequestSent(Friendship f) { if (currentTab == LeftTab.SEARCH) handleSearch(); }
  public void onFriendAccepted(Friendship f) {
    if (currentTab == LeftTab.FRIENDS) loadFriends();
    if (currentTab == LeftTab.REQUESTS) loadFriendRequests();
  }

  private void appendBubble(ChatMessage msg) {
    boolean mine = msg.getSenderId() == myId();
    Label sender = new Label(msg.getSenderUsername() != null ? msg.getSenderUsername() : "User");
    sender.getStyleClass().add("msg-sender");
    Label text = new Label(msg.getContent());
    text.getStyleClass().add("msg-text"); text.setWrapText(true); text.setMaxWidth(350);
    Label time = new Label(msg.getCreatedAt() != null ? msg.getCreatedAt().format(TIME_FMT) : "");
    time.getStyleClass().add("msg-time");
    VBox bubble = new VBox(2, sender, text, time);
    bubble.getStyleClass().add(mine ? "msg-bubble-mine" : "msg-bubble"); bubble.setMaxWidth(380);
    HBox row = new HBox(bubble);
    row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT); row.setMaxWidth(Double.MAX_VALUE);
    messagesContainer.getChildren().add(row);
  }

  private void scrollBottom() { Platform.runLater(() -> messagesScroll.setVvalue(1.0)); }
  private int myId() { return ClientSession.getCurrentUser() != null ? ClientSession.getCurrentUser().getId() : 0; }
  private String myName() { return ClientSession.getCurrentUser() != null ? ClientSession.getCurrentUser().getUsername() : ""; }

  private void async(Request req, java.util.function.Consumer<Response> callback) {
    Thread t = new Thread(() -> { Response res = NetworkClient.getInstance().sendRequestAndWait(req); if (res != null) callback.accept(res); });
    t.setDaemon(true); t.start();
  }

  private class LeftListCell extends ListCell<Object> {
    @Override
    protected void updateItem(Object item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) { setText(null); setGraphic(null); return; }
      if (currentTab == LeftTab.FRIENDS && item instanceof Friendship f) {
        boolean iAmRequester = f.getRequesterId() == myId();
        Label nameLabel = new Label(iAmRequester ? f.getAddresseeUsername() : f.getRequesterUsername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600;");
        setText(null); setGraphic(new HBox(10, nameLabel));
      } else if (currentTab == LeftTab.REQUESTS && item instanceof Friendship f) {
        Label nameLabel = new Label(f.getRequesterUsername() != null ? f.getRequesterUsername() : "User");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        Button acceptBtn = new Button("Accept");
        acceptBtn.getStyleClass().add("accept-btn"); acceptBtn.setOnAction(e -> acceptFriend(f.getRequesterId()));
        Button declineBtn = new Button("Decline");
        declineBtn.getStyleClass().add("decline-btn"); declineBtn.setOnAction(e -> declineFriend(f.getRequesterId()));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, nameLabel, spacer, acceptBtn, declineBtn);
        row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(2, 4, 2, 0));
        setText(null); setGraphic(row);
      } else if (currentTab == LeftTab.SEARCH && item instanceof User u) {
        Label nameLabel = new Label(u.getUsername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        String fullName = u.getFullName();
        Label subLabel = new Label(fullName != null && !fullName.isBlank() ? fullName : (u.getEmail() != null ? u.getEmail() : ""));
        subLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");
        Button addBtn = new Button("+ Add");
        addBtn.getStyleClass().add("add-friend-btn"); addBtn.setOnAction(e -> addFriend(u.getId()));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, new VBox(1, nameLabel, subLabel), spacer, addBtn);
        row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(2, 4, 2, 0));
        setText(null); setGraphic(row);
      } else { setText(item.toString()); setGraphic(null); }
    }
  }
}
