package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.util.FXThread;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Popup;

public class NotificationBell extends StackPane {
  public static String NORMAL = "🔔";
  public static String WIN = "🎉";
  public static String OUT_BID = "💸";
  public static String WARN = "⚠️";

  private final Button toggleButton = new Button("\uD83D\uDD14");

  private final Popup popupOverlay = new Popup();
  private final VBox overlayPane = new VBox();
  private final VBox listNotification = new VBox();

  public NotificationBell() {
    initToggleButton();
    initPopupOverlay();
    initListNotification();
  }

  private void initPopupOverlay() {
    overlayPane.setPrefSize(400, 350);
    overlayPane.setMinWidth(200);
    overlayPane.setMinHeight(150);
    overlayPane.setStyle("""
          -fx-background-color: #242526;
          -fx-border-color: black;
          -fx-border-radius: 10;
          -fx-background-radius: 10;
          -fx-padding: 10;
      """);

    Label label = new Label("NOTIFICATION");
    label.setStyle("""
      -fx-text-fill: white;
      -fx-font-weight: 800;
      """);
    overlayPane.getChildren().add(label);
    popupOverlay.getContent().add(overlayPane);
    popupOverlay.setAutoHide(true);
  }

  private void initListNotification() {
    listNotification.setAlignment(Pos.TOP_LEFT);
    listNotification.setSpacing(5);
    overlayPane.getChildren().add(listNotification);
  }

  private void initToggleButton() {
    toggleButton.setStyle("""
          -fx-min-width: 40px;
          -fx-min-height: 40px;
          -fx-max-width: 40px;
          -fx-max-height: 40px;
      
          -fx-background-radius: 20px;
          -fx-border-radius: 20px;
      
          -fx-background-color: #FFC107; 
          -fx-text-fill: #121212;
          -fx-graphic-text-gap: 0px;
          -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.2), 5, 0, 0, 2);
          -fx-cursor: hand;
      """);
    toggleButton.setOnAction(e -> {
      if (popupOverlay.isShowing()) {
        popupOverlay.hide();
      } else {
        double screenX = toggleButton.localToScreen(toggleButton.getBoundsInLocal()).getMinX();
        double screenY = toggleButton.localToScreen(toggleButton.getBoundsInLocal()).getMaxY() + 5;
        popupOverlay.show(toggleButton.getScene().getWindow(), screenX, screenY);
      }
    });
    getChildren().add(toggleButton);
    StackPane.setAlignment(toggleButton, Pos.TOP_LEFT);
    toggleButton.needsLayoutProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal) {
        this.setPrefSize(toggleButton.getWidth(), toggleButton.getHeight());
        this.setMaxSize(toggleButton.getWidth(), toggleButton.getHeight());
      }
    });
  }


  public void addNotification(String icon, String message) {
    FXThread.run(() -> {
      HBox notiItem = new HBox();
      notiItem.setAlignment(javafx.geometry.Pos.TOP_LEFT);
      notiItem.setSpacing(12.0);

      Label lblIcon = new Label(icon);

      Label lblMessage = new Label(message);
      lblMessage.setWrapText(true);

      HBox.setHgrow(lblMessage, javafx.scene.layout.Priority.ALWAYS);
      notiItem.getChildren().addAll(lblIcon, lblMessage);

      notiItem.setStyle("""
        -fx-background-color: #858789;
        -fx-border-color: black;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
        -fx-padding: 10;
        -fx-text-fill: white;
        """);
      listNotification.getChildren().add(0, notiItem);
    });
  }
}