package com.auction.client.ui.homeview.controller.admin;

import com.auction.client.app.AutoInject;
import com.auction.client.service.admin.AdminService;
import com.auction.client.store.userinformation.UserModel;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.FXThread;
import com.auction.client.util.StarUtils;
import com.auction.shared.User;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class UserManagementController implements CanRefresh {
  private FilteredList<UserModel> filteredUsers;

  private final AdminService adminService;

  @FXML private TableView<UserModel> userTable;
  @FXML private TableColumn<UserModel, String> colUsername;
  @FXML private TableColumn<UserModel, String> colEmail;
  @FXML private TableColumn<UserModel, String> colRole;
  @FXML private TableColumn<UserModel, String> colStatus;
  @FXML private TableColumn<UserModel, String> colRating;

  @FXML private ComboBox<String> ratingBox;

  private Runnable onRefresh;

  @AutoInject
  public UserManagementController(AdminService adminService) {
    this.adminService = adminService;
  }

  @FXML
  private void initialize() {
    setUpTable();
    setUpFilter();
  }

  private void setUpTable() {
    colUsername.setCellValueFactory(cell -> cell.getValue().usernameProperty());
    colEmail.setCellValueFactory(cell -> cell.getValue().emailProperty());
    colRole.setCellValueFactory(cell -> cell.getValue().roleProperty().asString());
    colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
    colRating.setCellValueFactory(cell -> cell.getValue().avgRatingProperty());
  }

  private void setUpFilter() {
    ratingBox.getItems().setAll(StarUtils.ratingList);
    ratingBox.setValue(StarUtils.ratingList.getFirst());
    ratingBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null && !newValue.equals(oldValue)) {
        filterRating(newValue);
      }
    });
  }

  public void setFilteredUsers(ObservableList<UserModel> baseUsers) {
    userTable.getItems().clear();
    this.filteredUsers = new FilteredList<>(baseUsers);
    userTable.setItems(filteredUsers);
  }

  private void filterRating(String rating) {
    if (StarUtils.ratingList.getFirst().equals(rating)) {
      filteredUsers.setPredicate(null);
    } else {
      filteredUsers.setPredicate(user ->
        StarUtils.getRatingTypeFromAvg(user.getUser().getAvgRating()).equals(rating)
      );
    }
  }

  @FXML
  private void handleBan() {
    UserModel selectedModel = userTable.getSelectionModel().getSelectedItem();
    if (selectedModel == null || selectedModel.getUser() == null) {
      AlertUtil.showWarningAlert("WARNING", "SELECT AN USER");
      return;
    }

    User user = userTable.getSelectionModel().getSelectedItem().getUser();
    if (user != null) {
      adminService.lockUser(user)
        .thenAccept(success -> {
          if (!success) {
            FXThread.run(() -> AlertUtil.showErrorAlert("Error", "failed to lock user"));
          }
      });
    }
  }

  @FXML
  private void handleUnban() {
    UserModel selectedModel = userTable.getSelectionModel().getSelectedItem();
    if (selectedModel == null || selectedModel.getUser() == null) {
      AlertUtil.showWarningAlert("WARNING", "SELECT AN USER");
      return;
    }

    User user = userTable.getSelectionModel().getSelectedItem().getUser();
    if (user != null) {
      adminService.unlockUser(user)
        .thenAccept(success -> {
          if (!success) {
            FXThread.run(() -> AlertUtil.showErrorAlert("Error", "failed to unlock user"));
          }
        });
    }
  }

  @FXML
  private void handlePromoteAdmin() {
    UserModel selectedModel = userTable.getSelectionModel().getSelectedItem();
    if (selectedModel == null || selectedModel.getUser() == null) {
      AlertUtil.showWarningAlert("WARNING", "SELECT AN USER");
      return;
    }

    User user = userTable.getSelectionModel().getSelectedItem().getUser();

    if (user == null) return;
    boolean isAdmin = user.getRole() == com.auction.shared.UserRole.ADMIN;
    String title = isAdmin ? "Demote from Admin" : "Promote to Admin";
    String question = (isAdmin ? "demote '" : "promote '") + user.getUsername() + (isAdmin ? "' back to Bidder?" : "' to Admin?");
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle(title);
    confirm.setHeaderText(null);
    confirm.setContentText("Are you sure you want to " + question);
    if (confirm.showAndWait().filter(b -> b == javafx.scene.control.ButtonType.OK).isPresent()) {
      String newRole = isAdmin ? com.auction.shared.UserRole.BIDDER.name() : com.auction.shared.UserRole.ADMIN.name();
      adminService.toggleAdmin(user, newRole)
        .thenAccept(success -> {
          if (!success) {
            FXThread.run(() -> AlertUtil.showErrorAlert("Error", "failed to toggle admin"));
          } else {
            FXThread.run(() -> refreshData());
          }
        });
    }
  }

  public void setOnRefresh(Runnable onRefresh) {
    this.onRefresh = onRefresh;
  }

  @Override
  public void refreshData() {
    if(onRefresh != null) {
      onRefresh.run();
    }
  }
}
