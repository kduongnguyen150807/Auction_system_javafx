package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.shared.TransactionLog;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TransactionHistory extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/history/TransactionHistory.fxml";

  @FXML
  private TableView<TransactionLog> table;
  @FXML
  private TableColumn<TransactionLog, String> idcol;
  @FXML
  private TableColumn<TransactionLog, String> typecol;
  @FXML
  private TableColumn<TransactionLog, String> amountcol;
  @FXML
  private TableColumn<TransactionLog, String> itemcol;
  @FXML
  private TableColumn<TransactionLog, String> datecol;

  public TransactionHistory() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  public void initialize() {
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
  }

  public void setTransactionHistory(ObservableList<TransactionLog> transactionLogs) {
    table.setItems(transactionLogs);
  }
}
