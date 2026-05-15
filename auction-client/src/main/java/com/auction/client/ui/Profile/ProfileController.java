package com.auction.client.ui.Profile;

import com.auction.client.ClientSession;
import com.auction.client.SceneManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.network.NetworkEventListener;
import com.auction.client.service.LotSubmissionService;
import com.auction.client.service.UserAccountService;
import com.auction.client.ui.Main.KhungController;
import com.auction.client.util.ImagePresentationUtil;
import com.auction.shared.*;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileController implements NetworkEventListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

  @FXML private ImageView avatarimageview;
  @FXML private Label usernameLabel, fullNameLabel, emailLabel, phoneLabel, roleLabel, balanceLabel;
  @FXML private Label moneySpentLabel, itemsBoughtLabel, moneyReceivedLabel, itemsSoldLabel;
  @FXML private Label ratingStarsLabel, ratingCountLabel, reputationWarning, verifiedLabel;
  @FXML private TextField fullNameInput, emailInput, phoneInput, DepositAmountField;
  @FXML private Button editButton, toggleRoleButton;
  @FXML private HBox bidderMetricsRow, sellerMetricsRow;

  private boolean editing = false;
  private final UserAccountService userAccountService = new UserAccountService();
  private final LotSubmissionService lotSubmissionService = new LotSubmissionService();

  @FXML
  public void initialize() {
    NetworkClient.getInstance().addListener(this);
    refreshData();
    setEditingMode(false);
  }

  @Override public void onBalanceUpdate(User user) { ClientSession.setCurrentUser(user); refreshData(); }

  @FXML
  public void handleDeposit() {
    try {
      double amount = Double.parseDouble(DepositAmountField.getText());
      int userId = ClientSession.getCurrentUser().getId();
      if (userAccountService.deposit(userId, amount)) refreshData();
    } catch (Exception e) {}
  }

  @FXML
  public void handleToggleRole() { ClientSession.toggleRole(); refreshData(); KhungController.refreshSidebarFromSession(); }

  private void refreshData() {
    User u = ClientSession.getCurrentUser();
    UserRole role = ClientSession.getActiveRole();
    if (u == null || role == null) return;
    usernameLabel.setText(u.getUsername()); fullNameLabel.setText(u.getFullName());
    emailLabel.setText(u.getEmail()); phoneLabel.setText(u.getPhoneNumber()); roleLabel.setText(role.name());
    if (verifiedLabel != null) {
      boolean verified = u.getPhoneNumber() != null && !u.getPhoneNumber().trim().isEmpty();
      verifiedLabel.setText(verified ? "\u2713 Verified" : "Unverified");
      verifiedLabel.setStyle(verified
          ? "-fx-background-color: #44cc44; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 12; -fx-font-weight: bold;"
          : "-fx-background-color: #ff4444; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 12; -fx-font-weight: bold;");
    }
    if (balanceLabel != null) balanceLabel.setText(String.format("%,.0f$", u.getBalance()));
    if (moneySpentLabel != null) moneySpentLabel.setText(String.format("%,.0f$", u.getMoneySpent()));
    if (itemsBoughtLabel != null) itemsBoughtLabel.setText(String.valueOf(u.getItemsBought()));
    if (moneyReceivedLabel != null) moneyReceivedLabel.setText(String.format("%,.0f$", u.getMoneyReceived()));
    if (itemsSoldLabel != null) itemsSoldLabel.setText(String.valueOf(u.getItemsSold()));
    toggleRoleButton.setVisible(u.getRole() != UserRole.ADMIN);
    toggleRoleButton.setText(role == UserRole.SELLER ? "Switch to Bidder" : "Switch to Seller");
    boolean isBidder = role == UserRole.BIDDER;
    if (bidderMetricsRow != null) { bidderMetricsRow.setVisible(isBidder); bidderMetricsRow.setManaged(isBidder); }
    if (sellerMetricsRow != null) { sellerMetricsRow.setVisible(!isBidder); sellerMetricsRow.setManaged(!isBidder); }
    if (ratingStarsLabel != null) {
      if (u.getTotalRatings() > 0) {
        int stars = (int) Math.round(u.getAvgRating());
        String starStr = "\u2605".repeat(stars) + "\u2606".repeat(5 - stars);
        String sentiment = u.getAvgRating() <= 2.0 ? "Negative" : (u.getAvgRating() <= 3.0 ? "Neutral" : "Positive");
        String color = u.getAvgRating() <= 2.0 ? "-fx-text-fill: #ff4444;" : (u.getAvgRating() <= 3.0 ? "-fx-text-fill: #ffaa00;" : "-fx-text-fill: #44cc44;");
        ratingStarsLabel.setText(starStr); ratingStarsLabel.setStyle("-fx-font-size: 16; " + color);
        ratingCountLabel.setText(String.format("%.1f (%d ratings) - %s", u.getAvgRating(), u.getTotalRatings(), sentiment));
      } else { ratingStarsLabel.setText(""); ratingCountLabel.setText("No ratings yet"); }
    }
    if (reputationWarning != null) {
      boolean warn = u.getAvgRating() < 2.0 && u.getTotalRatings() >= 3;
      reputationWarning.setText(warn ? "WARNING: Low reputation user" : "");
      reputationWarning.setVisible(warn); reputationWarning.setManaged(warn);
    }
    if (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty())
      ImagePresentationUtil.loadCircularAvatar(avatarimageview, u.getAvatarUrl(), 34, 68);
  }

  private void setEditingMode(boolean v) {
    editing = v;
    editButton.setText(editing ? "Save" : "Edit Profile");
    fullNameLabel.setVisible(!v); fullNameLabel.setManaged(!v);
    fullNameInput.setVisible(v); fullNameInput.setManaged(v);
    emailLabel.setVisible(!v); emailLabel.setManaged(!v);
    emailInput.setVisible(v); emailInput.setManaged(v);
    phoneLabel.setVisible(!v); phoneLabel.setManaged(!v);
    phoneInput.setVisible(v); phoneInput.setManaged(v);
    if (v) {
      User u = ClientSession.getCurrentUser();
      if (u != null) {
        fullNameInput.setText(u.getFullName() != null ? u.getFullName() : "");
        emailInput.setText(u.getEmail() != null ? u.getEmail() : "");
        phoneInput.setText(u.getPhoneNumber() != null ? u.getPhoneNumber() : "");
      }
    }
  }

  @FXML
  public void handleEditProfile() {
    if (editing) {
      User current = ClientSession.getCurrentUser();
      if (current == null) return;
      String err = userAccountService.updateProfile(
          current.getId(), fullNameInput.getText(), emailInput.getText(), phoneInput.getText());
      if (err == null) { refreshData(); KhungController.refreshSidebarFromSession(); }
      else new Alert(Alert.AlertType.ERROR, err).showAndWait();
    }
    setEditingMode(!editing);
  }
  //Anti-ghosting vẫn gửi thông báo về desktop client dù đã đăng xuất
  @FXML
  public void handleLogout() {
    ClientSession.clear();
    Request req = new Request("LOGOUT", null);
    NetworkClient.getInstance().sendRequestAndWait(req);
    try {
      SceneManager.switchScene("/fxml/welcome.fxml");
    } catch (IOException e) {
    }
  }

  @FXML
  public void handleChangeAvatar() throws Exception {
    javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
    java.io.File file = fc.showOpenDialog(null);
    if (file != null) {
      byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
      String url = lotSubmissionService.uploadImage("https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload", bytes);
      userAccountService.updateAvatar(ClientSession.getUsername(), url);
      ImagePresentationUtil.loadCircularAvatar(avatarimageview, url, 34, 68);
    }
  }

  /** Load latest user profile from server and update UI (non-blocking). */
  public void refreshFromServer() {
    if (ClientSession.getCurrentUser() == null) return;
    int userId = ClientSession.getCurrentUser().getId();
    Thread t =
        new Thread(
            () -> {
              try {
                User refreshed = userAccountService.refreshUser(userId);
                if (refreshed != null) {
                  ClientSession.setCurrentUser(refreshed);
                  Platform.runLater(this::refreshData);
                }
              } catch (Exception e) {
                LOGGER.warn("refresh_user failed for id={}", userId, e);
              }
            });
    t.setDaemon(true);
    t.start();
  }

  public void handleRefresh(javafx.event.ActionEvent event) {
    refreshFromServer();
  }

  @FXML
  public void handleShowHistory() {
    try {
      javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/history/TransactionHistory.fxml"));
      javafx.scene.Parent root = loader.load();
      javafx.stage.Stage stage = new javafx.stage.Stage();
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.initStyle(StageStyle.TRANSPARENT);
      stage.setTitle("Lịch sử giao dịch");
      Scene scene = new Scene(root, 800, 500);


      scene.setFill(Color.TRANSPARENT);
      stage.setScene(scene);

      stage.focusedProperty().addListener((obs, oldVal, focused) -> {
        if (!focused) {
          stage.close();
        }
      });
      stage.show();
    } catch (Exception e) { LOGGER.warn("Failed to open transaction history window", e); }
  }
}
