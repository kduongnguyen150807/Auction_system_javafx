package com.auction.client.ui.homeview.controller.admin;

import com.auction.client.app.AutoInject;
import com.auction.client.service.admin.AdminService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.FXThread;
import com.auction.shared.Item;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ItemManagementController {
  private FilteredList<ItemModel> items;

  private final AdminService adminService;

  @FXML private TableView<ItemModel> pendingItem;
  @FXML private TableColumn<ItemModel, String> colItemName;
  @FXML private TableColumn<ItemModel, String> colItemSeller;
  @FXML private TableColumn<ItemModel, String> colItemPrice;
  @FXML private TableColumn<ItemModel, String> colItemCategory;

  @AutoInject
  public ItemManagementController(AdminService adminService) {
    this.adminService = adminService;
  }

  @FXML
  private void initialize() {
    setUpTable();
  }

  private void setUpTable() {
    colItemName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
    colItemSeller.setCellValueFactory(
      cellData -> new SimpleStringProperty(cellData.getValue().getItem().getSellerUsername()));
    colItemPrice.setCellValueFactory(cellData -> cellData.getValue().currentPriceProperty().asString());
    colItemCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
  }

  public void setPendingItem(ObservableList<ItemModel> items) {
    pendingItem.getItems().clear();
    this.items = new FilteredList<>(items);
    pendingItem.setItems(items);
  }

  @FXML
  private void handleApprove() {
    ItemModel itemModel = pendingItem.getSelectionModel().getSelectedItem();
    if (itemModel == null) {
      AlertUtil.showWarningAlert("WARNING", "SELECT AN ITEM");
      return;
    }

    Item item = pendingItem.getSelectionModel().getSelectedItem().getItem();
    if (item == null) return;
    adminService.approveItem(item)
      .thenAccept(success -> {
        if (!success) {
          FXThread.run(() -> AlertUtil.showWarningAlert("WARNING", "ERROR"));
        } else {
          FXThread.run(() -> AlertUtil.showInfoAlert("INFO", "SUCCESS"));
        }
      });
  }

  @FXML
  private void handleReject() {
    ItemModel itemModel = pendingItem.getSelectionModel().getSelectedItem();
    if (itemModel == null) {
      AlertUtil.showWarningAlert("WARNING", "SELECT AN ITEM");
      return;
    }

    Item item = pendingItem.getSelectionModel().getSelectedItem().getItem();
    if (item == null) return;
    adminService.rejectItem(item)
      .thenAccept(success -> {
        if (!success) {
          FXThread.run(() -> AlertUtil.showWarningAlert("WARNING", "ERROR"));
        } else {
          FXThread.run(() -> AlertUtil.showInfoAlert("INFO", "SUCCESS"));
        }
      });
  }
}
