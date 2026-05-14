package com.auction.client.ui.maindashboard.itemInformation;

import com.auction.client.app.UILoader;
import com.auction.client.navigation.SceneManager;
import com.auction.client.service.AuctionService;
import com.auction.client.ui.component.BidDialog;
import com.auction.shared.dto.BidForm;
import com.auction.shared.item.Item;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ItemInformationController {
  @FXML private ItemInfoLayoutController infoLayoutController;
  @FXML private ItemButtonLayoutController buttonLayoutController;

  @FXML
  private void initialize() {
    setFocusedItem();

    buttonLayoutController.setOnBid(() -> showBidDialog());
  }

  void setFocusedItem() {
    infoLayoutController.setFocusedItem(
      AuctionService.getInstance().getFocusedItemProperty());
  }

  private void showBidDialog() {
    BidDialog bidDialog = new BidDialog();
    bidDialog.setData(AuctionService.getInstance().getFocusedItem());
    bidDialog.setOnSubmit(() -> {
      BidForm bidForm = bidDialog.collectData(AuctionService.getInstance().getFocusedItem());
      if (bidForm != null) {
        placeBid(bidForm);
        bidDialog.closeWindow();
      } else {
        bidDialog.showError("INVALID AMOUNT");
      }
    });
    UILoader.showModal(bidDialog, SceneManager.getInstance().getWindow());
  }

  private void placeBid(BidForm bidForm) {
    AuctionService.getInstance().placeBid(bidForm);
  }
}
