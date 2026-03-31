package com.auction.client.ui.Profile;

import com.auction.client.ClientSession;
import com.auction.client.SceneManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.UserRole;
import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileController {
  @FXML private Label usernameLabel;
  @FXML private Label fullNameLabel;
  @FXML private Label emailLabel;
  @FXML private Label phoneLabel;
  @FXML private Label roleLabel;
  @FXML private Label metricOneTitleLabel;
  @FXML private Label metricOneValueLabel;
  @FXML private Label metricTwoTitleLabel;
  @FXML private Label metricTwoValueLabel;
  @FXML private TextField fullNameInput;
  @FXML private TextField emailInput;
  @FXML private TextField phoneInput;
  @FXML private Button editButton;
  @FXML private Button toggleRoleButton;

  private boolean editing = false;

  @FXML
  public void initialize() {
    refreshData();
    setEditingMode(false);
  }

  @FXML
  public void handleEditProfile() {
    if (!editing) {
      setEditingMode(true);
      return;
    }
    ClientSession.updateProfile(
        fullNameInput.getText(), emailInput.getText(), phoneInput.getText());
    refreshData();
    setEditingMode(false);
    KhungController.refreshSidebarFromSession();
  }

  @FXML
  public void handleToggleRole() {
    ClientSession.toggleRole();
    refreshData();
    KhungController.refreshSidebarFromSession();
  }

  @FXML
  public void handleLogout() {
    ClientSession.clear();
    try {
      SceneManager.switchscene("/fxml/welcome.fxml");
    } catch (IOException ignored) {
    }
  }

  public void refreshData() {
    String username = fallback(ClientSession.getUsername(), "username");
    String fullName = fallback(ClientSession.getFullName(), username);
    String email = fallback(ClientSession.getEmail(), "username@mail.com");
    String phone = fallback(ClientSession.getPhone(), "N/A");
    UserRole role = ClientSession.getActiveRole();

    usernameLabel.setText(username);
    fullNameLabel.setText(fullName);
    emailLabel.setText(email);
    phoneLabel.setText(phone);
    roleLabel.setText(toTitle(role.name()));

    fullNameInput.setText(fullName);
    emailInput.setText(email);
    phoneInput.setText(phone);

    toggleRoleButton.setText(role == UserRole.SELLER ? "Switch to Bidder" : "Switch to Seller");
    refreshRoleMetrics(role);
  }

  private void setEditingMode(boolean value) {
    editing = value;
    editButton.setText(editing ? "Save Profile" : "Edit Profile");

    fullNameLabel.setVisible(!editing);
    fullNameLabel.setManaged(!editing);
    emailLabel.setVisible(!editing);
    emailLabel.setManaged(!editing);
    phoneLabel.setVisible(!editing);
    phoneLabel.setManaged(!editing);

    fullNameInput.setVisible(editing);
    fullNameInput.setManaged(editing);
    emailInput.setVisible(editing);
    emailInput.setManaged(editing);
    phoneInput.setVisible(editing);
    phoneInput.setManaged(editing);
  }

  private String fallback(String value, String fallback) {
    return (value == null || value.isBlank()) ? fallback : value;
  }

  private String toTitle(String role) {
    String lower = role.toLowerCase();
    return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
  }

  private void refreshRoleMetrics(UserRole role) {
    if (ClientSession.getCurrentUser() == null) {
      applyMetrics(role, 0, 0.0);
      return;
    }

    final int userId = ClientSession.getCurrentUser().getid();
    Thread worker =
        new Thread(
            () -> {
              try {
                NetworkClient.getinstance().sendrequest(new Request(Request.list, null));
                Response res = NetworkClient.getinstance().receiveresponse();
                if (res == null || !Response.ok.equals(res.getstatus())) {
                  Platform.runLater(() -> applyMetrics(role, 0, 0.0));
                  return;
                }
                Object payload = res.getpayload();
                if (!(payload instanceof List<?> rawItems)) {
                  Platform.runLater(() -> applyMetrics(role, 0, 0.0));
                  return;
                }

                int count = 0;
                double money = 0.0;
                for (Object obj : rawItems) {
                  if (!(obj instanceof Item item)) continue;
                  if (role == UserRole.BIDDER) {
                    if (item.getstatus() == ItemStatus.FINISHED && item.getwinnerid() == userId) {
                      count++;
                      money += item.getcurrentprice();
                    }
                  } else if (role == UserRole.SELLER) {
                    if (item.getstatus() == ItemStatus.FINISHED
                        && item.getsellerid() == userId
                        && item.getwinnerid() > 0) {
                      count++;
                      money += item.getcurrentprice();
                    }
                  }
                }

                final int finalCount = count;
                final double finalMoney = money;
                Platform.runLater(() -> applyMetrics(role, finalCount, finalMoney));
              } catch (Exception ignored) {
                Platform.runLater(() -> applyMetrics(role, 0, 0.0));
              }
            });
    worker.setDaemon(true);
    worker.start();
  }

  private void applyMetrics(UserRole role, int itemCount, double moneyAmount) {
    if (role == UserRole.BIDDER) {
      metricOneTitleLabel.setText("ITEMS BOUGHT");
      metricOneValueLabel.setText(String.valueOf(itemCount));
      metricTwoTitleLabel.setText("MONEY SPENT");
      metricTwoValueLabel.setText(formatMoney(moneyAmount));
      return;
    }

    metricOneTitleLabel.setText("ITEMS SOLD");
    metricOneValueLabel.setText(String.valueOf(itemCount));
    metricTwoTitleLabel.setText("MONEY RECEIVED");
    metricTwoValueLabel.setText(formatMoney(moneyAmount));
  }

  private String formatMoney(double value) {
    return String.format(java.util.Locale.US, "%.2f$", value);
  }
}
