package com.auction.client.ui.homeview.controller.admin;

import com.auction.client.service.admin.AdminService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.util.AlertUtil;
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

  private AdminService adminService;

  @FXML private TableView<ItemModel> pendingItem;
  @FXML private TableColumn<ItemModel, String> colItemName;
  @FXML private TableColumn<ItemModel, String> colItemSeller;
  @FXML private TableColumn<ItemModel, String> colItemPrice;
  @FXML private TableColumn<ItemModel, String> colItemCategory;

  @FXML
  private void initialize() {
    setUpTable();
  }

  public void setAdminService(AdminService adminService) {
    this.adminService = adminService;
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
    Item item = pendingItem.getSelectionModel().getSelectedItem().getItem();
    if (item == null) return;
    boolean success = adminService.approveItem(item);
    if (!success) {
      AlertUtil.showErrorAlert("ERROR", "FAIL TO APPROVE ITEM");
    } else {
      AlertUtil.showInfoAlert("INFO", "SUCCESS TO APPROVE ITEM");
    }
  }

  @FXML
  private void handleReject() {
    Item item = pendingItem.getSelectionModel().getSelectedItem().getItem();
    if (item == null) return;
    boolean success = adminService.rejectItem(item);
    if (!success) {
      AlertUtil.showErrorAlert("ERROR", "FAIL TO REJECT ITEM");
    } else {
      AlertUtil.showInfoAlert("INFO", "SUCCESS TO REJECT ITEM");
    }
  }
}
