package com.auction.client.ui.maindashboard.admin;

import com.auction.client.service.AdminService;
import com.auction.client.service.AuctionService;
import com.auction.client.store.AuctionStore;
import com.auction.client.ui.base.CanRefresh;
import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PendingItemController implements CanRefresh {
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
      new SimpleStringProperty(cell.getValue().getName())
    );

    startingPriceColumn.setCellValueFactory(cell ->
      new SimpleStringProperty(cell.getValue().getName())
    );

    categoryColumn.setCellValueFactory(cell ->
      new SimpleStringProperty(cell.getValue().getName())
    );
  }

  @FXML
  private void handleRefresh() {
    refreshData();
  }

  @FXML
  private void handleApprove() {}

  @FXML
  private void handleReject() {}

  @FXML
  private void handleViewDetail() {}

  @Override
  public void refreshData() {
    AuctionService.getInstance().refreshItem();
    pendingTable.setItems(
      AuctionService.getInstance().getItemsByStatus(ItemStatus.PENDING)
    );
  }

  public void setService(AdminService service) {
    this.adminService = service;
  }
}
