package com.auction.client.util;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

public class NotificationPopup {
  private Popup popup;

  public NotificationPopup() {
    popup = new Popup();
    popup.setAutoHide(true);

    VBox container = new VBox();
    container.setStyle(
        "-fx-background-color: #242526; -fx-border-color: #3e4042; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 5);");
    container.setPrefSize(350, 450);

    Label titleLabel = new Label("Thông Báo Của Tôi");
    titleLabel.setStyle(
        "-fx-text-fill: #ffaa00; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 15;");

    ListView<String> listView = new ListView<>(NotificationCenter.getNotifications());
    listView.setStyle(
        "-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 5;");
    listView.setPrefHeight(400);

    listView.setCellFactory(
        lv ->
            new ListCell<String>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle("-fx-background-color: transparent;");
                } else {
                  setText(item);
                  setStyle(
                      "-fx-text-fill: #e4e6eb; -fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #242526; -fx-border-color: #3e4042; -fx-border-width: 0 0 1 0;");
                  setOnMouseEntered(
                      e ->
                          setStyle(
                              "-fx-text-fill: #ffffff; -fx-background-color: #3a3b3c; -fx-cursor: hand;"));
                  setOnMouseExited(
                      e ->
                          setStyle(
                              "-fx-text-fill: #e4e6eb; -fx-background-color: #242526; -fx-border-color: #3e4042; -fx-border-width: 0 0 1 0;"));
                }
              }
            });

    container.getChildren().addAll(titleLabel, listView);
    popup.getContent().add(container);
  }

  public void show(Window owner, double x, double y) {
    if (popup.isShowing()) popup.hide();
    else popup.show(owner, x, y);
  }
}
