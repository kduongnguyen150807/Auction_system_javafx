package com.auction.client.ui.Chat;

import com.auction.shared.Friendship;
import com.auction.shared.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Renders rows in the chat sidebar (friends, requests, user search). */
final class ChatLeftListCell extends ListCell<Object> {

  private final ChatLeftListHost host;

  ChatLeftListCell(ChatLeftListHost host) {
    this.host = host;
  }

  @Override
  protected void updateItem(Object item, boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
      setGraphic(null);
      return;
    }
    ChatSidebarTab tab = host.currentSidebarTab();
    int myId = host.currentUserId();
    if (tab == ChatSidebarTab.FRIENDS && item instanceof Friendship f) {
      boolean iAmRequester = f.getRequesterId() == myId;
      Label nameLabel = new Label(iAmRequester ? f.getAddresseeUsername() : f.getRequesterUsername());
      nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600;");
      setText(null);
      setGraphic(new HBox(10, nameLabel));
    } else if (tab == ChatSidebarTab.REQUESTS && item instanceof Friendship f) {
      Label nameLabel = new Label(f.getRequesterUsername() != null ? f.getRequesterUsername() : "User");
      nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
      Button acceptBtn = new Button("Accept");
      acceptBtn.getStyleClass().add("accept-btn");
      acceptBtn.setOnAction(e -> host.acceptFriend(f.getRequesterId()));
      Button declineBtn = new Button("Decline");
      declineBtn.getStyleClass().add("decline-btn");
      declineBtn.setOnAction(e -> host.declineFriend(f.getRequesterId()));
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      HBox row = new HBox(8, nameLabel, spacer, acceptBtn, declineBtn);
      row.setAlignment(Pos.CENTER_LEFT);
      row.setPadding(new Insets(2, 4, 2, 0));
      setText(null);
      setGraphic(row);
    } else if (tab == ChatSidebarTab.SEARCH && item instanceof User u) {
      Label nameLabel = new Label(u.getUsername());
      nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
      String fullName = u.getFullName();
      Label subLabel =
          new Label(
              fullName != null && !fullName.isBlank()
                  ? fullName
                  : (u.getEmail() != null ? u.getEmail() : ""));
      subLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");
      Button addBtn = new Button("+ Add");
      addBtn.getStyleClass().add("add-friend-btn");
      addBtn.setOnAction(e -> host.addFriend(u.getId()));
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      HBox row = new HBox(8, new VBox(1, nameLabel, subLabel), spacer, addBtn);
      row.setAlignment(Pos.CENTER_LEFT);
      row.setPadding(new Insets(2, 4, 2, 0));
      setText(null);
      setGraphic(row);
    } else {
      setText(item.toString());
      setGraphic(null);
    }
  }
}
