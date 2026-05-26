package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.app.AutoInject;
import com.auction.client.navigation.SceneManager;
import com.auction.client.service.auction.BiddingService;
import com.auction.client.service.item.ItemService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.ui.component.IntegerField;
import com.auction.client.ui.component.RatingForm;
import com.auction.client.ui.homeview.homeviewcomponent.BiddingForm;
import com.auction.client.ui.homeview.homeviewcomponent.RatingBox;
import com.auction.client.ui.homeview.homeviewcomponent.VBoxModel;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.FXThread;
import com.auction.client.util.StageUtil;
import com.auction.shared.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class ButtonLayoutController {
  private ItemModel selectedItem;
  private Item item;

  private List<Rating> cachedRatings = new ArrayList<>();

  @FXML private Button bidButton;
  @FXML private Button autoBidButton;
  @FXML private Button rateButton;
  @FXML private ComboBox<String> ratingFilterCombo;
  @FXML private VBox ratingsContainer;
  @FXML private IntegerField autoBidField;

  private final BiddingService biddingService;
  private final ItemService itemService;

  @AutoInject
  public ButtonLayoutController(BiddingService biddingService, ItemService itemService) {
    this.biddingService = biddingService;
    this.itemService = itemService;
  }

  public void setSelectedItem(ItemModel selectedItem) {
    this.selectedItem = selectedItem;
    this.item = selectedItem.getItem();

    selectedItem.statusProperty().addListener((observable, oldValue, newStatus) -> {
      FXThread.run(() -> adjustButton(newStatus));
    });

    adjustButton(selectedItem.getStatus());
  }

  @FXML
  private void showBiddingForm() {
    if (item == null) return;

    BiddingForm biddingForm = new BiddingForm(item);
    biddingForm.setOnConfirm(() -> {
      double bidAmount = biddingForm.collectData();
      User currentUser = ClientSession.CURRENT_SESSION.getCurrentUser();
      if (!validate(currentUser, bidAmount)) {
        return;
      }
      placeBid(bidAmount);
    });
    StageUtil.showModalStage(biddingForm, SceneManager.getInstance().getWindow());
  }

  @FXML
  private void handleAutoBid() {
    double autoBidAmount = autoBidField.getValue();
    User currentUser = ClientSession.CURRENT_SESSION.getCurrentUser();
    if (!validate(currentUser, autoBidAmount)) {
      return;
    }
    placeAutoBid(autoBidAmount);
  }

  private void adjustButton(ItemStatus itemStatus) {
    if (ItemStatus.OPEN.equals(itemStatus)) {
      enableButton();
    } else if (ItemStatus.CLOSED.equals(itemStatus) || ItemStatus.EXPIRED.equals(itemStatus) || ItemStatus.FINISHED.equals(itemStatus)) {
      disableButton();
    }
  }

  private void disableButton() {
    bidButton.setDisable(true);
    autoBidButton.setDisable(true);
    autoBidField.setDisable(true);

    ratingsContainer.setVisible(true);
    ratingsContainer.setManaged(true);

    boolean canrate = (item.getStatus() == ItemStatus.CLOSED || item.getStatus() == ItemStatus.FINISHED)
      && ClientSession.CURRENT_SESSION.getUser() != null
      && ClientSession.CURRENT_SESSION.getUser().getId() == item.getWinnerId();
    rateButton.setVisible(canrate);
    rateButton.setManaged(canrate);

    loadRatings();
  }

  private void enableButton() {
    bidButton.setDisable(false);
    autoBidButton.setDisable(false);
    autoBidField.setDisable(false);

    ratingsContainer.setVisible(false);
    rateButton.setVisible(false);
    ratingsContainer.setManaged(false);
    rateButton.setManaged(false);
  }

  private boolean validate(User user, double bidAmount) {
    if (user == null) {
      AlertUtil.showErrorAlert("Not logged in", "Please log in first.");
      return false;
    }
    String phone = user.getPhoneNumber();
    if (phone == null || phone.trim().isEmpty()) {
      AlertUtil.showErrorAlert(
        "Unverified Account",
        "You must add a phone number in your Profile to place a bid.");
      return false;
    }
    if (bidAmount <= 0) {
      AlertUtil.showErrorAlert("Invalid amount", "You must enter a valid amount.");
      return false;
    }
    return true;
  }

  private void placeBid(double bidAmount) {
    BidTransaction tx = new BidTransaction(
      item.getId(),
      ClientSession.CURRENT_SESSION.getCurrentUser().getId(),
      bidAmount
    );

    setFormDisabled(true);

    biddingService.placeBid(tx)
      .thenAccept(response -> FXThread.run(() -> {
        setFormDisabled(false);
        handleResponseAlert(response);
      }))
      .exceptionally(ex -> {
        FXThread.run(() -> {
          setFormDisabled(false);
          AlertUtil.showErrorAlert("Connection Error", "Failed to send bid request.");
        });
        return null;
      });
  }

  private void placeAutoBid(double autoBidAmount) {
    BidTransaction bid = new BidTransaction(
      item.getId(),
      ClientSession.CURRENT_SESSION.getCurrentUser().getId(),
      0
    );
    bid.setMaxAutoBid(autoBidAmount);
    bid.setAutoBid(true);

    setFormDisabled(true);

    biddingService.placeBid(bid)
      .thenAccept(response -> FXThread.run(() -> {
        setFormDisabled(false);
        handleResponseAlert(response);
      }))
      .exceptionally(ex -> {
        FXThread.run(() -> {
          setFormDisabled(false);
          AlertUtil.showErrorAlert("Connection Error", "Failed to send auto-bid request.");
        });
        return null;
      });
  }

  private void setFormDisabled(boolean disabled) {
    bidButton.setDisable(disabled);
    autoBidButton.setDisable(disabled);
    autoBidField.setDisable(disabled);
  }

  private void handleResponseAlert(Response response) {
    if (Response.OK.equals(response.getStatus())) {
      AlertUtil.showInfoAlert("Bidding Success", response.getMessage());
    } else {
      AlertUtil.showWarningAlert("Bidding Refused", response.getMessage());
    }
  }

  @FXML
  private void showRatingForm() {
    RatingForm form = new RatingForm(selectedItem.getId());

    form.setOnSubmit(() -> {
      Rating rating = form.collectData();
      if (rating == null) {
        AlertUtil.showErrorAlert("Rating", "PLEASE SELECT AT LEAST 1 STAR");
      }
      if (rating != null) {
        itemService.submitRating(rating)
          .thenCompose(v -> {
            FXThread.run(() -> {
              rateButton.setVisible(false);
              rateButton.setManaged(false);
              loadRatings();
            });
            return null;
          });
      }
    });

    StageUtil.showModalStage(form, SceneManager.getInstance().getWindow());
  }

  private void loadRatings() {
    if (selectedItem.getId() <= 0) {
      return;
    }

    itemService.loadRatings(selectedItem.getId())
      .thenAccept(list -> {
        for (Rating r : list) {
          if (r.getRaterUserId() == ClientSession.CURRENT_SESSION.getCurrentUser().getId()) {
            rateButton.setVisible(false);
            rateButton.setManaged(false);
            break;
          }
        }
        cachedRatings.clear();
        cachedRatings.addAll(list);
        FXThread.run(() -> {
          if (ratingFilterCombo != null && ratingFilterCombo.getItems().isEmpty()) {
            ratingFilterCombo.getItems().addAll("All", "Positive", "Neutral", "Negative");
            ratingFilterCombo.setValue("All");
          }
          renderratings("All");
        });
      });
  }

  @FXML
  private void handleRatingFilter() {
    String filter = ratingFilterCombo != null && ratingFilterCombo.getValue() != null ? ratingFilterCombo.getValue() : "All";
    renderratings(filter);
  }

  private void renderratings(String filter) {
    RatingListRenderer.render(ratingsContainer, cachedRatings, filter);
  }
}