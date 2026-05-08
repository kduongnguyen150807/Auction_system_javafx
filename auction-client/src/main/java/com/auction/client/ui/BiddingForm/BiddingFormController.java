package com.auction.client.ui.BiddingForm;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeManager;
import com.auction.client.service.BiddingClientService;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.BidTransaction;
import com.auction.shared.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class BiddingFormController {
  @FXML private Pane RootPane;
  @FXML private Label ItemId;
  @FXML private Label ItemName;
  @FXML private Label MaxPriceInfo;
  @FXML private TextField BidAmount;
  private int itemId = -1;
  private ItemInformationController parent;
  private final BiddingClientService biddingClientService = new BiddingClientService();

  @FXML
  private void removeForm() {
    NodeManager.removeNodeFromPane(RootPane, KhungController.getMainContentPane());
  }

  public void setData(int itemId, String itemname, double maxPrice) {
    this.itemId = itemId;
    if (ItemId != null) ItemId.setText(String.valueOf(itemId));
    if (ItemName != null) ItemName.setText(itemname == null ? "" : itemname);
    if (MaxPriceInfo != null) {
      if (maxPrice > 0) MaxPriceInfo.setText(String.format("Buy it now price: %,.0f$", maxPrice));
      else MaxPriceInfo.setText("No instant buy option");
    }
  }

  public void setParentController(ItemInformationController p) {
    this.parent = p;
  }

  @FXML
  private void handleConfirmBidding() {
    try {
      if (ClientSession.getCurrentUser() == null) {
        showAlert(Alert.AlertType.WARNING, "Not logged in", "Please login to place a bid.");
        return;
      }
      String phone = ClientSession.getCurrentUser().getPhoneNumber();
      if (phone == null || phone.trim().isEmpty()) {
        showAlert(
            Alert.AlertType.WARNING,
            "Unverified Account",
            "You must add a phone number in your Profile to place a bid.");
        return;
      }
      if (itemId <= 0) {
        showAlert(Alert.AlertType.ERROR, "Missing item", "Item not selected.");
        return;
      }
      String raw = BidAmount != null ? BidAmount.getText() : null;
      if (raw == null) raw = "";
      raw = raw.replace("$", "").replace(",", "").trim();
      if (raw.isBlank()) {
        showAlert(Alert.AlertType.WARNING, "Invalid amount", "Enter a bid amount.");
        return;
      }
      double ans = Double.parseDouble(raw);
      BidTransaction res = new BidTransaction(itemId, ClientSession.getCurrentUser().getId(), ans);
      Response res2 = biddingClientService.placeBid(res);

      if (res2 != null && Response.OK.equals(res2.getStatus())) {
        Object res3 = res2.getPayload();
        if (res3 instanceof BidTransaction) {
          double ans2 = ((BidTransaction) res3).getBidValue();
          if (parent != null) {
            parent.updateCurrentBid(ans2);
          }
        }
        removeForm();

        if ("BUY_IT_NOW_SUCCESS".equals(res2.getMessage())) {
          if (parent != null) {
            parent.markAsSold();
          }
          showAlert(
              Alert.AlertType.INFORMATION,
              "CHỐT ĐƠN",
              "Mày đã hốt trọn món này! Hãy ấn 'Refresh' ở trang chủ để làm mới.");
        } else {
          showAlert(Alert.AlertType.INFORMATION, "Success", "Bid placed successfully.");
        }
      } else {
        String ans3 = res2 != null ? res2.getMessage() : "Failed to place bid.";
        showAlert(Alert.AlertType.ERROR, "Bid failed", ans3);
      }
    } catch (Exception e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Invalid bid amount.");
    }
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
    try {
      Alert res = new Alert(type);
      res.setTitle(title);
      res.setHeaderText(null);
      res.setContentText(content);
      res.showAndWait();
    } catch (Exception ignored) {
    }
  }
}
