package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.app.AutoInject;
import com.auction.client.navigation.SceneManager;
import com.auction.client.service.auction.BiddingService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.ui.component.IntegerField;
import com.auction.client.ui.homeview.homeviewcomponent.BiddingForm;
import com.auction.client.ui.homeview.homeviewcomponent.RatingBox;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.FXThread;
import com.auction.client.util.StageUtil;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Response;
import com.auction.shared.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ButtonLayoutController {
  private ItemModel selectedItem;
  private Item item;

  @FXML private Button bidButton;
  @FXML private Button autoBidButton;
  @FXML private RatingBox ratingBox;
  @FXML private IntegerField autoBidField;

  private final BiddingService biddingService;

  @AutoInject
  public ButtonLayoutController(BiddingService biddingService) {
    this.biddingService = biddingService;
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
    } else if (ItemStatus.CLOSED.equals(itemStatus)) {
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
}