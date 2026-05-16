package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.ClientSession;
import com.auction.client.navigation.SceneManager;
import com.auction.client.service.BiddingService;
import com.auction.client.store.ClientItem;
import com.auction.client.ui.component.IntegerField;
import com.auction.client.ui.homeview.homeviewcomponent.BiddingForm;
import com.auction.client.ui.homeview.homeviewcomponent.RatingBox;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.StageUtil;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ButtonLayoutController {
  private ClientItem selectedItem;
  private Item item;

  @FXML private Button bidButton;
  @FXML private Button autoBidButton;
  @FXML private RatingBox ratingBox;
  @FXML private IntegerField autoBidField;

  public void setSelectedItem(ClientItem selectedItem) {
    this.selectedItem = selectedItem;
    this.item = selectedItem.getItem();

    /* add listener to item status, which help disable or enable button */
    selectedItem.statusProperty().addListener((observable, oldValue, newStatus) -> {
      adjustButton(newStatus);
    });

    adjustButton(selectedItem.getStatus());
  }

  @FXML
  private void showBiddingForm() {
    if (item != null) {
      BiddingForm biddingForm = new BiddingForm(item);
      biddingForm.setOnConfirm(() -> {
        double bidAmount = biddingForm.collectData();
        User currentUser = ClientSession.getCurrentUser();
        if (!validate(currentUser, bidAmount)) {
          return;
        }
        placeBid(bidAmount, currentUser);
      });
      StageUtil.showModalStage(biddingForm, SceneManager.getInstance().getWindow());
    }
  }

  @FXML
  private void handleAutoBid() {
    double autoBidAmount = autoBidField.getValue();
    User currentUser = ClientSession.getCurrentUser();
    if (!validate(currentUser, autoBidAmount)) {
      return;
    }
    placeAutoBid(autoBidAmount, currentUser);
  }

  private void adjustButton(ItemStatus itemStatus) {
    if (itemStatus.equals(ItemStatus.OPEN)) {
      enableButton();
    } else if (itemStatus.equals(ItemStatus.CLOSED)) {
      disableButton();
    }
  }

  private void disableButton() {
    bidButton.setDisable(true);
    autoBidButton.setDisable(true);
    autoBidField.setDisable(true);
    ratingBox.setVisible(true);
  }

  private void enableButton() {
    bidButton.setDisable(false);
    autoBidButton.setDisable(false);
    autoBidField.setDisable(false);
    ratingBox.setVisible(false);
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

  private void placeBid(double bidAmount, User user) {
    BidTransaction res = new BidTransaction(
      item.getId(),
      ClientSession.getCurrentUser().getId(),
      bidAmount);
    String message = BiddingService.placeBid(res);
    AlertUtil.showInfoAlert("Bidding result", message);
  }

  private void placeAutoBid(double autoBidAmount, User user) {
    BidTransaction bid = new BidTransaction(
      item.getId(),
      ClientSession.getCurrentUser().getId(),
      0
    );
    bid.setMaxAutoBid(autoBidAmount);
    bid.setAutoBid(true);
    String message = BiddingService.placeBid(bid);
    AlertUtil.showInfoAlert("Bidding result", message);
  }
}
