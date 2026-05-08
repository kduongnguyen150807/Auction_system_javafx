package com.auction.client.ui.Chat;

import com.auction.shared.ChatMessage;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Builds a single chat row (bubble + alignment) for the messages list. */
final class ChatBubbleRowFactory {

  private ChatBubbleRowFactory() {}

  static HBox createRow(ChatMessage msg, boolean mine, DateTimeFormatter timeFmt) {
    Label sender = new Label(msg.getSenderUsername() != null ? msg.getSenderUsername() : "User");
    sender.getStyleClass().add("msg-sender");
    Label text = new Label(msg.getContent());
    text.getStyleClass().add("msg-text");
    text.setWrapText(true);
    text.setMaxWidth(350);
    Label time =
        new Label(msg.getCreatedAt() != null ? msg.getCreatedAt().format(timeFmt) : "");
    time.getStyleClass().add("msg-time");
    VBox bubble = new VBox(2, sender, text, time);
    bubble.getStyleClass().add(mine ? "msg-bubble-mine" : "msg-bubble");
    bubble.setMaxWidth(380);
    HBox row = new HBox(bubble);
    row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
    row.setMaxWidth(Double.MAX_VALUE);
    return row;
  }
}
