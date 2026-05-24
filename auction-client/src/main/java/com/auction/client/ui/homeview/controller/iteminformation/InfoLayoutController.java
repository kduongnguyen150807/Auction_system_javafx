package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.app.AutoInject;
import com.auction.client.service.user.UserService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.util.FXThread;
import com.auction.client.util.ImageViewUtils;
import com.auction.client.util.StringFormat;
import com.auction.client.util.TimeFormat;
import com.auction.shared.ItemStatus;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class InfoLayoutController {
  private ItemModel selectedItem;

  private Timeline countdownTimeline;

  @FXML private Label itemName;
  @FXML private ImageView sellerAvatar;
  @FXML private Label sellerName;
  @FXML private Label itemDescription;
  @FXML private Label currentHighestBidValue;
  @FXML private Label maxPriceValue;
  @FXML private Label endsInValue;

  private final UserService userService;

  @AutoInject
  public InfoLayoutController(UserService userService) {
    this.userService = userService;
  }

  public void setSelectedItem(ItemModel clientItem) {
    unbind();
    this.selectedItem = clientItem;
    loadSellerAvatar(clientItem.getItem().getSellerAvatarUrl());
    bind();
  }

  private void unbind() {
    if (countdownTimeline != null) {
      countdownTimeline.stop();
      countdownTimeline = null;
    }

    itemName.textProperty().unbind();
    itemDescription.textProperty().unbind();
    currentHighestBidValue.textProperty().unbind();
    endsInValue.textProperty().unbind();
  }

  private void bind() {
    if (selectedItem == null) {
      return;
    }

    itemName.textProperty().bind(selectedItem.nameProperty());
    itemDescription.textProperty().bind(selectedItem.descriptionProperty());
    currentHighestBidValue.textProperty().bind(selectedItem.currentPriceProperty().asString("$ %.2f"));
    maxPriceValue.setText(StringFormat.formatMoney(selectedItem.getItem().getMaxPrice()));
    sellerName.setText(selectedItem.getItem().getSellerUsername());

    if (selectedItem.getItem().getStatus() == ItemStatus.OPEN && !selectedItem.getItem().getStartTime().isAfter(LocalDateTime.now())) {
      setUpCountdownTimeline(selectedItem.getItem().getEndTime());
    } else if (selectedItem.getItem().getStartTime().isAfter(LocalDateTime.now())) {
      endsInValue.setText("UPCOMING IN: " + TimeFormat.getDHM(selectedItem.getItem().getStartTime()));
    } else {
      if (selectedItem.getItem().getWinnerId() != 0) {
        userService.getUserById(selectedItem.getItem().getWinnerId())
            .thenCompose(user -> {
              FXThread.run(() -> {
                endsInValue.setText("WINNER: " + user.getUsername());
              });
              return null;
            });
      } else {
        endsInValue.setText("EXPIRED");
      }
    }
  }

  private void setUpCountdownTimeline(LocalDateTime endTime) {
    if (endTime == null) {
      endsInValue.setText("UNKNOW TIME");
      return;
    }
    long totalSecondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), endTime);
    if (totalSecondsLeft <= 0) {
      endsInValue.setText("WINNER: " + selectedItem.getItem().getWinnerUsername());
    }
    if (totalSecondsLeft < 3600) {
      countdownTimeline = new Timeline(
        new KeyFrame(Duration.seconds(1), event -> {
          long currentSecondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), endTime);
          if (currentSecondsLeft <= 0) {
            endsInValue.setText("CLOSED");
            countdownTimeline.stop();
          } else {
            endsInValue.setText(TimeFormat.getDHMS(endTime));
          }
        })
      );
      countdownTimeline.setCycleCount(Timeline.INDEFINITE);
      countdownTimeline.play();
    } else {
      endsInValue.textProperty().bind(Bindings.createStringBinding(
        () -> TimeFormat.getDHM(selectedItem.getItem().getEndTime()),
        selectedItem.endTimeProperty()
      ));
    }
  }

  private void loadSellerAvatar(String url) {
    if (sellerAvatar == null) {
      return;
    }

    if (url == null || url.isBlank()) {
      sellerAvatar.setImage(null);
      return;
    }

    ImageViewUtils.setImageToImageView(sellerAvatar, url);
  }
}
