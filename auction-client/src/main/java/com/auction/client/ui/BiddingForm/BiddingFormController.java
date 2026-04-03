package com.auction.client.ui.BiddingForm;
import com.auction.client.ClientSession;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.BidTransaction;
import com.auction.shared.Request;
import com.auction.shared.Response;
import javafx.application.Platform;
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
    private int itemid = -1;
    private Label bidvaluelabel;
    @FXML
    private void RemoveForm() {
        NodeManager.removeNodeFromPane(RootPane, KhungController.getKhungChua());
    }
    public void setData(int itemid, String itemname) {
        this.itemid = itemid;
        if (ItemId != null) ItemId.setText(String.valueOf(itemid));
        if (ItemName != null) ItemName.setText(itemname == null ? "" : itemname);
    }
    public void setbidvaluelabel(Label bidvaluelabel) {
        this.bidvaluelabel = bidvaluelabel;
    }
    @FXML
    private void HandleConfirmBidding() {
        try {
            if (ClientSession.getCurrentUser() == null) {
                showAlert(Alert.AlertType.WARNING, "Not logged in", "Please login to place a bid.");
                return;
            }
            if (itemid <= 0) {
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
            BidTransaction res = new BidTransaction(itemid, ClientSession.getCurrentUser().getid(), ans);
            Request req = new Request(Request.bid, res);
            Response res2 = NetworkClient.getinstance().sendrequestandwait(req);
            if (res2 != null && Response.ok.equals(res2.getstatus())) {
                Object res3 = res2.getpayload();
                if (res3 instanceof BidTransaction) {
                    double ans2 = ((BidTransaction) res3).getbidvalue();
                    if (bidvaluelabel != null) {
                        Platform.runLater(() -> bidvaluelabel.setText(String.format("%,.2f$", ans2)));
                    }
                }
                RemoveForm();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Bid placed successfully.");
            } else {
                String ans3 = res2 != null ? res2.getmessage() : "Failed to place bid.";
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
