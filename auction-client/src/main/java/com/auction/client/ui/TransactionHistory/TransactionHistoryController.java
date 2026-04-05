package com.auction.client.ui.TransactionHistory;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.TransactionLog;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TransactionHistoryController {
  @FXML private TableView<TransactionLog> table;
  @FXML private TableColumn<TransactionLog, String> idcol;
  @FXML private TableColumn<TransactionLog, String> typecol;
  @FXML private TableColumn<TransactionLog, String> amountcol;
  @FXML private TableColumn<TransactionLog, String> itemcol;
  @FXML private TableColumn<TransactionLog, String> datecol;

  @FXML
  public void initialize() {
    try {
      idcol.setCellValueFactory(
          res ->
              new javafx.beans.property.SimpleStringProperty(
                  String.valueOf(res.getValue().getId())));
      typecol.setCellValueFactory(
          res -> new javafx.beans.property.SimpleStringProperty(res.getValue().getType()));
      amountcol.setCellValueFactory(
          res ->
              new javafx.beans.property.SimpleStringProperty(
                  String.format("%.2f$", res.getValue().getAmount())));
      itemcol.setCellValueFactory(
          res ->
              new javafx.beans.property.SimpleStringProperty(
                  String.valueOf(res.getValue().getItemId())));
      datecol.setCellValueFactory(
          res ->
              new javafx.beans.property.SimpleStringProperty(
                  res.getValue().getCreatedAt() != null
                      ? res.getValue().getCreatedAt().toString()
                      : "N/A"));
      loadData();
    } catch (Exception e) {
    }
  }

  private void loadData() {
    if (ClientSession.getCurrentUser() == null) return;
    int res = ClientSession.getCurrentUser().getId();
    Request req = new Request("get_transactions", res);
    Response ans = NetworkClient.getInstance().sendRequestAndWait(req);
    if (ans != null && Response.OK.equals(ans.getStatus())) {
      try {
        List<TransactionLog> res1 = (List<TransactionLog>) ans.getPayload();
        if (res1 != null) {
          ObservableList<TransactionLog> ans1 = FXCollections.observableArrayList(res1);
          table.setItems(ans1);
        }
      } catch (Exception e) {
      }
    }
  }
}
