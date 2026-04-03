package com.auction.client.ui.BiddingForm;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.BidTransaction;
import com.auction.shared.Request;
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
  @FXML private TextField BidAmount;

  private int itemId = -1;

  @FXML
  private void RemoveForm() {
    NodeManager.removeNodeFromPane(RootPane, KhungController.getKhungChua());
  }

  public void setData(int itemId, String itemName) {
    this.itemId = itemId;
    if (ItemId != null) ItemId.setText(String.valueOf(itemId));
    if (ItemName != null) ItemName.setText(itemName == null ? "" : itemName);
  }

  @FXML
  private void HandleConfirmBidding() {
    try {
      if (ClientSession.getCurrentUser() == null) {
        showAlert(Alert.AlertType.WARNING, "Not logged in", "Please login to place a bid.");
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
      double bidValue = Double.parseDouble(raw);

      BidTransaction transaction =
          new BidTransaction(itemId, ClientSession.getCurrentUser().getid(), bidValue);
      Request req = new Request(Request.bid, transaction);
      Response res = NetworkClient.getinstance().sendrequestandwait(req);

      if (res != null && Response.ok.equals(res.getstatus())) {
        RemoveForm();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Bid placed successfully.");
      } else {
        String msg = res != null ? res.getmessage() : "Failed to place bid.";
        showAlert(Alert.AlertType.ERROR, "Bid failed", msg);
      }
    } catch (Exception e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Invalid bid amount.");
    }
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
    try {
      Alert alert = new Alert(type);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(content);
      alert.showAndWait();
    } catch (Exception ignored) {
    }
  }
}
