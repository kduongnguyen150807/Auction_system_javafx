package com.auction.client.ui.maindashboard.admin;

import com.auction.client.service.AdminService;
import javafx.fxml.FXML;

public class AdminDashboardController {
  private AdminService adminService = new AdminService();

  @FXML private UserManagementController userManagementController;
  @FXML private PendingItemController pendingItemController;
  @FXML private AnalyticsController analyticsController;

  @FXML
  public void initialize() {
    loadAllModules();
    refreshDashboard();
  }

  private void loadAllModules() {
    pendingItemController.setService(adminService);
  }

  private void refreshDashboard() {
    pendingItemController.refreshData();
  }
}

