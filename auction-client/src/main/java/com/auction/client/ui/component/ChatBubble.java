package com.auction.client.ui.component;

import com.auction.shared.ChatMessage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ChatBubble extends HBox {
  private static final String FXML_PATH = "/fxml/component/ChatBubble.fxml";

  @FXML private VBox bubble;
  @FXML private Label senderLabel;
  @FXML private Label textLabel;
  @FXML private Label timeLabel;

  private DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public ChatBubble(ChatMessage msg) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML_PATH));
    fxmlLoader.setController(this);
    fxmlLoader.setRoot(this);

    try {
      fxmlLoader.load();
      setupBubble(msg);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setupBubble(ChatMessage msg) {
    textLabel.setText(msg.getContent());
    timeLabel.setText(msg.getCreatedAt() != null ? msg.getCreatedAt().format(timeFmt) : "");

    HBox.setHgrow(bubble, Priority.NEVER);
  }

  public void setPlace(boolean isMine, ChatMessage msg) {
    if (isMine) {
      this.setAlignment(Pos.CENTER_RIGHT);
      bubble.getStyleClass().add("msg-bubble-mine");
      timeLabel.setAlignment(Pos.CENTER_RIGHT);

      senderLabel.setVisible(false);
      senderLabel.setManaged(false);
    } else {
      this.setAlignment(Pos.CENTER_LEFT);
      bubble.getStyleClass().add("msg-bubble");
      timeLabel.setAlignment(Pos.CENTER_LEFT);

      senderLabel.setText(msg.getSenderUsername() != null ? msg.getSenderUsername() : "User");
    }
  }
}