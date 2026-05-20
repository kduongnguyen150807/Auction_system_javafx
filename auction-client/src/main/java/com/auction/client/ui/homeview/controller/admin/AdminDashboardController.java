package com.auction.client.ui.homeview.controller.admin;

import com.auction.client.app.AutoInject;
import com.auction.client.service.admin.AdminService;
import com.auction.client.service.item.ItemService;
import com.auction.client.service.user.UserService;
import com.auction.client.store.lotsinformation.PendingLots;
import com.auction.client.store.userinformation.UsersList;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.util.AlertUtil;
import javafx.fxml.FXML;

public class AdminDashboardController implements CanRefresh {
  @FXML private UserManagementController userManagementController;
  @FXML private ItemManagementController itemManagementController;

  private final AdminService adminService;
  private final UserService userService;
  private final ItemService itemService;

  @AutoInject
  public AdminDashboardController(AdminService adminService, UserService userService, ItemService itemService) {
    this.adminService = adminService;
    this.userService = userService;
    this.itemService = itemService;
  }

  @FXML
  private void initialize() {
    apply();
  }

  @Override
  public void refreshData() {
    boolean userSuccess = userService.getAllUsers();
    if (!userSuccess) {
      AlertUtil.showErrorAlert("Error", "failed to fetch all users");
    }

    boolean itemSuccess = itemService.loadPendingItem();
    if (!itemSuccess) {
      AlertUtil.showErrorAlert("Error", "failed to fetch items");
    }
  }

  private void apply() {
    userManagementController.setFilteredUsers(UsersList.USER_LIST.getUsers());
    itemManagementController.setPendingItem(PendingLots.PENDING_LOTS.getPendingItemsList());

    userManagementController.setAdminService(adminService);
    itemManagementController.setAdminService(adminService);

    userManagementController.setOnRefresh(this::refreshData);
  }
}
