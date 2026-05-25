package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.service.user.UserService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.util.FXThread;
import com.auction.client.util.ImageViewUtils;
import com.auction.client.util.StringFormat;
import com.auction.client.util.TimeFormat;
import com.auction.shared.ItemStatus;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

public class InfoLayoutController {

  private ItemModel selectedItem;

  private Timeline countdownTimeline;
  private PauseTransition upcomingRefreshTransition;

  @FXML private Label itemName;
  @FXML private ImageView sellerAvatar;
  @FXML private Label sellerName;
  @FXML private Label itemDescription;
  @FXML private Label currentHighestBidValue;
  @FXML private Label maxPriceValue;
  @FXML private Label endsInValue;

  private final UserService userService;
  private final AuctionDiscoveryService discoveryService;
  private final AuctionDetailService detailService;

  private final ChangeListener<ItemStatus> statusChangeListener = (observable, oldValue, newValue) -> {
    if (newValue != null) {
      FXThread.run(() -> {
        handleStatusChange(newValue);
      });
    }
  };

  private final ChangeListener<LocalDateTime> endTimeChangeListener = (observable, oldValue, newValue) -> {
    if (selectedItem == null || selectedItem.getItem() == null || newValue == null) return;
    handleStatusChange(selectedItem.statusProperty().get());
  };

  @AutoInject
  public InfoLayoutController(
    UserService userService,
    AuctionDiscoveryService discoveryService,
    AuctionDetailService detailService
  ) {
    this.userService = userService;
    this.discoveryService = discoveryService;
    this.detailService = detailService;
  }

  public void setSelectedItem(ItemModel itemModel) {
    unbind();
    this.selectedItem = itemModel;

    if (itemModel == null || itemModel.getItem() == null) {
      clearUI();
      return;
    }

    loadSellerAvatar(itemModel.getItem().getSellerAvatarUrl());
    bind();
  }

  private void bind() {
    if (selectedItem == null || selectedItem.getItem() == null) return;

    itemName.textProperty().bind(selectedItem.nameProperty());
    itemDescription.textProperty().bind(selectedItem.descriptionProperty());
    currentHighestBidValue.textProperty().bind(selectedItem.currentPriceProperty().asString("$ %.2f"));
    maxPriceValue.setText(StringFormat.formatMoney(selectedItem.getItem().getMaxPrice()));
    sellerName.setText(selectedItem.getItem().getSellerUsername());

    selectedItem.statusProperty().addListener(statusChangeListener);
    selectedItem.endTimeProperty().addListener(endTimeChangeListener);

    handleStatusChange(selectedItem.statusProperty().get());
  }

  private void unbind() {
    stopTimers();
    endsInValue.textProperty().unbind();
    itemName.textProperty().unbind();
    itemDescription.textProperty().unbind();
    currentHighestBidValue.textProperty().unbind();

    if (selectedItem != null) {
      selectedItem.statusProperty().removeListener(statusChangeListener);
      selectedItem.endTimeProperty().removeListener(endTimeChangeListener);
    }
  }

  private void clearUI() {
    itemName.setText("");
    itemDescription.setText("");
    currentHighestBidValue.setText("");
    maxPriceValue.setText("");
    sellerName.setText("");
    endsInValue.setText("");
    sellerAvatar.setImage(null);
  }

  private void stopTimers() {
    if (countdownTimeline != null) {
      countdownTimeline.stop();
      countdownTimeline = null;
    }
    if (upcomingRefreshTransition != null) {
      upcomingRefreshTransition.stop();
      upcomingRefreshTransition = null;
    }
  }

  private void handleStatusChange(ItemStatus status) {
    stopTimers();
    endsInValue.textProperty().unbind();

    System.out.println("Status: " + status);

    if (selectedItem == null || selectedItem.getItem() == null) {
      endsInValue.setText("");
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startTime = selectedItem.getItem().getStartTime();
    LocalDateTime endTime = selectedItem.endTimeProperty().get();

    if (startTime != null && now.isBefore(startTime)) {
      setUpUpcomingRefresh(startTime);
      return;
    }

    if (status == ItemStatus.EXPIRED) {
      endsInValue.setText("EXPIRED");
      return;
    }

    if (status == ItemStatus.CLOSED) {
      int winnerId = selectedItem.getItem().getWinnerId();
      if (winnerId != 0) {
        discoveryService.getUserById(winnerId).thenAccept(user -> {
          endsInValue.setText("WINNER: " + user.getUsername());
        });
      } else {
        endsInValue.setText("EXPIRED");
      }
      return;
    }

    if (status == ItemStatus.OPEN) {
      if (endTime != null) {
        setUpCountdownTimeline(endTime);
      } else {
        endsInValue.setText("NO END TIME");
      }
    }
  }

  private void setUpUpcomingRefresh(LocalDateTime startTime) {
    if (startTime == null) return;

    long secondsUntilStart = ChronoUnit.SECONDS.between(LocalDateTime.now(), startTime);

    if (secondsUntilStart <= 0) {
      endsInValue.setText("STARTING...");
      refreshItem();
      handleStatusChange(selectedItem.statusProperty().get());
      return;
    }

    endsInValue.setText("UPCOMING IN: " + TimeFormat.getDHM(startTime));

    upcomingRefreshTransition = new PauseTransition(Duration.seconds(secondsUntilStart));
    upcomingRefreshTransition.setOnFinished(event -> {
      endsInValue.setText("STARTING...");
      refreshItem();
      handleStatusChange(selectedItem.statusProperty().get());
    });
    upcomingRefreshTransition.play();
  }

  private void setUpCountdownTimeline(LocalDateTime endTime) {
    if (endTime == null) return;
    if (countdownTimeline != null) {
      countdownTimeline.stop();
    }

    updateCountdown(endTime);

    countdownTimeline = new Timeline(
      new KeyFrame(
        Duration.seconds(1),
        event -> updateCountdown(endTime)
      )
    );
    countdownTimeline.setCycleCount(Timeline.INDEFINITE);
    countdownTimeline.play();
  }

  private void updateCountdown(LocalDateTime endTime) {
    long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), endTime);

    if (secondsLeft <= 0) {
      endsInValue.setText("CLOSED");
      stopTimers();
      refreshItem();
      return;
    }

    if (secondsLeft >= 3600) {
      endsInValue.setText(TimeFormat.getDHM(endTime));
    } else {
      endsInValue.setText(TimeFormat.getDHMS(endTime));
    }
  }

  private void refreshItem() {
    if (selectedItem == null || selectedItem.getItem() == null) return;

    discoveryService.refreshItem(selectedItem.getItem().getId());
  }

  private void loadSellerAvatar(String url) {
    if (sellerAvatar == null) return;
    if (url == null || url.isBlank()) {
      sellerAvatar.setImage(null);
      return;
    }
    ImageViewUtils.setImageToImageView(sellerAvatar, url);
  }
}