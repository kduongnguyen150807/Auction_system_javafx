package com.auction.client.ui.maindashboard.admin;

import com.auction.client.service.AdminService;
import com.auction.client.service.AuctionService;
import com.auction.client.service.AuthService;
import com.auction.client.store.AuctionStore;
import com.auction.client.ui.base.CanRefresh;
import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PendingItemController implements CanRefresh {
  private static final Logger LOGGER = LoggerFactory.getLogger(PendingItemController.class);
  private AdminService adminService;

  @FXML private TableView<Item> pendingTable;
  @FXML private TableColumn<Item, String> itemNameColumn;
  @FXML private TableColumn<Item, String> sellerColumn;
  @FXML private TableColumn<Item, String> categoryColumn;
  @FXML private TableColumn<Item, String> startingPriceColumn;
  @FXML private TableColumn<Item, String> conditionColumn;
  @FXML private TableColumn<Item, String> createdAtColumn;

  @FXML private Button approveButton;
  @FXML private Button rejectButton;
  @FXML private Button viewDetailButton;

  @FXML
  public void initialize() {
    setupTable();
  }

  private void setupTable() {
    itemNameColumn.setCellValueFactory(cell ->
      new SimpleStringProperty(cell.getValue().getName())
    );

    sellerColumn.setCellValueFactory(cell ->
      new SimpleStringProperty(String.valueOf(cell.getValue().getSellerId()))
    );

    startingPriceColumn.setCellValueFactory(cell ->
      new SimpleStringProperty(String.valueOf(cell.getValue().getStartingPrice()))
    );

    categoryColumn.setCellValueFactory(cell ->
      new SimpleStringProperty(String.valueOf(cell.getValue().getCategory()))
    );

    conditionColumn.setCellValueFactory(cell ->
      new SimpleStringProperty(String.valueOf(cell.getValue().getStatus()))
    );

    createdAtColumn.setCellValueFactory(cell ->
      new SimpleStringProperty(String.valueOf(cell.getValue().getStartTime()))
    );
  }

  @FXML
  private void handleRefresh() {
    refreshData();
  }

  @FXML
  private void handleApprove() {
    adminService.handleApprove(pendingTable.getSelectionModel().getSelectedItem()).thenAccept(response -> {
      refreshData();
    });
  }

  @FXML
  private void handleReject() {}

  @FXML
  private void handleViewDetail() {}

  @Override
  public void refreshData() {
    AuctionService.getInstance().refreshItemv2().thenAccept(response -> {
      Platform.runLater(() -> setPendingItem(AuctionService.getInstance().
        getItemsByStatus(ItemStatus.PENDING)));
    });
  }

  public void setPendingItem(ObservableList<Item> items) {
    pendingTable.getItems().setAll(items);
  }

  public void setService(AdminService service) {
    this.adminService = service;
  }
}
