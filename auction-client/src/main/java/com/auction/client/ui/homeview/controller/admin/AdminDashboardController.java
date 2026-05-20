package com.auction.client.ui.homeview.controller.admin;

import com.auction.client.app.AutoInject;
import com.auction.client.service.admin.AdminService;
import com.auction.client.service.item.ItemService;
import com.auction.client.service.user.UserService;
import com.auction.client.store.lotsinformation.PendingLots;
import com.auction.client.store.userinformation.UsersList;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.util.FXThread;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AdminDashboardController implements CanRefresh {
  private static final Logger LOGGER = LoggerFactory.getLogger(AdminDashboardController.class);

  @FXML private UserManagementController userManagementController;
  @FXML private ItemManagementController itemManagementController;

  @FXML private PieChart statusChart;
  @FXML private BarChart categoryChart;

  private final UserService userService;
  private final ItemService itemService;
  private final AdminService adminService;

  @AutoInject
  public AdminDashboardController(AdminService adminService, UserService userService, ItemService itemService) {
    this.userService = userService;
    this.itemService = itemService;
    this.adminService = adminService;
  }

  @FXML
  private void initialize() {
    apply();
  }

  @Override
  public void refreshData() {
    CompletableFuture<Void> userFetch = userService.getAllUsers();
    CompletableFuture<Void> itemFetch = itemService.loadPendingItems();

    loadStats();
  }

  private void apply() {
    userManagementController.setFilteredUsers(UsersList.USER_LIST.getUsers());
    itemManagementController.setPendingItem(PendingLots.PENDING_LOTS.getPendingItemsList());

    userManagementController.setOnRefresh(this::refreshData);
  }

  @FXML
  private void loadStats() {

    adminService.getStatusStats()
      .thenAccept(map -> FXThread.run(() -> {
        if (statusChart == null) return;

        statusChart.getData().clear();
        if (map != null && !map.isEmpty()) {
          for (Map.Entry<String, Integer> e : map.entrySet()) {
            String label = e.getKey() + " (" + e.getValue() + ")";
            statusChart.getData().add(new PieChart.Data(label, e.getValue()));
          }
        }
      }))
      .exceptionally(ex -> {
        LOGGER.error("Lỗi khi nạp dữ liệu PieChart", ex);
        return null;
      });

    adminService.getCategoryStats()
      .thenAccept(map -> FXThread.run(() -> {
        if (categoryChart == null) return;

        categoryChart.getData().clear();
        if (map != null && !map.isEmpty()) {
          XYChart.Series<String, Number> series = new XYChart.Series<>();
          series.setName("Revenue ($)");

          for (Map.Entry<String, Double> e : map.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
          }
          categoryChart.getData().add(series);
        }
      }))
      .exceptionally(ex -> {
        LOGGER.error("Lỗi khi nạp dữ liệu BarChart", ex);
        return null;
      });
  }
}
