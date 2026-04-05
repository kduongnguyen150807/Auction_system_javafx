package com.auction.client.ui.Profile;

import com.auction.client.ClientSession;
import com.auction.client.SceneManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class ProfileController {
  @FXML private javafx.scene.image.ImageView avatarimageview;
  @FXML private Label usernameLabel, fullNameLabel, emailLabel, phoneLabel, roleLabel, balanceLabel;
  @FXML private Label moneySpentLabel, itemsBoughtLabel, moneyReceivedLabel, itemsSoldLabel;
  @FXML private Label ratingStarsLabel, ratingCountLabel, reputationWarning, verifiedLabel;
  @FXML private TextField fullNameInput, emailInput, phoneInput, DepositAmountField;
  @FXML private Button editButton, toggleRoleButton;
  @FXML private HBox bidderMetricsRow, sellerMetricsRow;

  private boolean editing = false;
  private static ProfileController instance;

  public static ProfileController getInstance() {
    return instance;
  }

  @FXML
  public void initialize() {
    instance = this;
    refreshData();
    setEditingMode(false);
  }

  public void updateBalanceDirectly(User u) {
    Platform.runLater(
        () -> {
          ClientSession.setCurrentUser(u);
          refreshData();
        });
  }

  @FXML
  public void handleDeposit() {
    try {
      double res = Double.parseDouble(DepositAmountField.getText());
      Map<String, String> ans = new HashMap<>();
      ans.put("userid", String.valueOf(ClientSession.getCurrentUser().getId()));
      ans.put("amount", String.valueOf(res));
      Request req = new Request("deposit", (java.io.Serializable) ans);
      Response ans2 = NetworkClient.getInstance().sendRequestAndWait(req);
      if (ans2 != null && Response.OK.equals(ans2.getStatus())) {
        ClientSession.setCurrentUser((User) ans2.getPayload());
        refreshData();
      }
    } catch (Exception e) {
    }
  }

  @FXML
  public void handleToggleRole() {
    ClientSession.toggleRole();
    refreshData();
    KhungController.refreshSidebarFromSession();
  }

  public void refreshData() {
    User res = ClientSession.getCurrentUser();
    UserRole ans = ClientSession.getActiveRole();
    if (res != null && ans != null) {
      usernameLabel.setText(res.getUsername());
      fullNameLabel.setText(res.getFullName());
      emailLabel.setText(res.getEmail());
      phoneLabel.setText(res.getPhoneNumber());
      roleLabel.setText(ans.name());
      if (verifiedLabel != null) {
        String ph = res.getPhoneNumber();
        boolean verified = ph != null && !ph.trim().isEmpty();
        verifiedLabel.setText(verified ? "\u2713 Verified" : "Unverified");
        verifiedLabel.setStyle(
            verified
                ? "-fx-background-color: #44cc44; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 12; -fx-font-weight: bold;"
                : "-fx-background-color: #ff4444; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 12; -fx-font-weight: bold;");
      }
      if (balanceLabel != null) balanceLabel.setText(String.format("%,.0f$", res.getBalance()));
      if (moneySpentLabel != null)
        moneySpentLabel.setText(String.format("%,.0f$", res.getMoneySpent()));
      if (itemsBoughtLabel != null) itemsBoughtLabel.setText(String.valueOf(res.getItemsBought()));
      if (moneyReceivedLabel != null)
        moneyReceivedLabel.setText(String.format("%,.0f$", res.getMoneyReceived()));
      if (itemsSoldLabel != null) itemsSoldLabel.setText(String.valueOf(res.getItemsSold()));

      toggleRoleButton.setVisible(res.getRole() != UserRole.ADMIN);
      toggleRoleButton.setText(ans == UserRole.SELLER ? "Switch to Bidder" : "Switch to Seller");

      boolean res2 = ans == UserRole.BIDDER;
      if (bidderMetricsRow != null) {
        bidderMetricsRow.setVisible(res2);
        bidderMetricsRow.setManaged(res2);
      }
      if (sellerMetricsRow != null) {
        sellerMetricsRow.setVisible(!res2);
        sellerMetricsRow.setManaged(!res2);
      }

      if (ratingStarsLabel != null) {
        if (res.getTotalRatings() > 0) {
          int res3 = (int) Math.round(res.getAvgRating());
          String res4 = "\u2605".repeat(res3) + "\u2606".repeat(5 - res3);
          String res6 =
              res.getAvgRating() <= 2.0
                  ? "Negative"
                  : (res.getAvgRating() <= 3.0 ? "Neutral" : "Positive");
          String res7 =
              res.getAvgRating() <= 2.0
                  ? "-fx-text-fill: #ff4444;"
                  : (res.getAvgRating() <= 3.0
                      ? "-fx-text-fill: #ffaa00;"
                      : "-fx-text-fill: #44cc44;");
          ratingStarsLabel.setText(res4);
          ratingStarsLabel.setStyle("-fx-font-size: 16; " + res7);
          ratingCountLabel.setText(
              String.format(
                  "%.1f (%d ratings) - %s", res.getAvgRating(), res.getTotalRatings(), res6));
        } else {
          ratingStarsLabel.setText("");
          ratingCountLabel.setText("No ratings yet");
        }
      }
      if (reputationWarning != null) {
        boolean res5 = res.getAvgRating() < 2.0 && res.getTotalRatings() >= 3;
        reputationWarning.setText(res5 ? "WARNING: Low reputation user" : "");
        reputationWarning.setVisible(res5);
        reputationWarning.setManaged(res5);
      }

      if (res.getAvatarUrl() != null && !res.getAvatarUrl().isEmpty()) {
        javafx.scene.image.Image ansimg = new javafx.scene.image.Image(res.getAvatarUrl(), true);
        ansimg
            .progressProperty()
            .addListener(
                (obs, oldv, newv) -> {
                  if (newv.doubleValue() == 1.0 && !ansimg.isError()) {
                    double resw = ansimg.getWidth();
                    double resh = ansimg.getHeight();
                    double ansmin = Math.min(resw, resh);
                    double resx = (resw - ansmin) / 2;
                    double resy = (resh - ansmin) / 2;
                    Platform.runLater(
                        () -> {
                          avatarimageview.setImage(ansimg);
                          avatarimageview.setViewport(
                              new javafx.geometry.Rectangle2D(resx, resy, ansmin, ansmin));
                          avatarimageview.setFitWidth(68);
                          avatarimageview.setFitHeight(68);
                          avatarimageview.setPreserveRatio(false);
                          double ansr = 34.0;
                          avatarimageview.setClip(new javafx.scene.shape.Circle(ansr, ansr, ansr));
                        });
                  }
                });
      }
    }
  }

  private void setEditingMode(boolean v) {
    editing = v;
    editButton.setText(editing ? "Save" : "Edit Profile");
    fullNameLabel.setVisible(!v);
    fullNameLabel.setManaged(!v);
    fullNameInput.setVisible(v);
    fullNameInput.setManaged(v);
    emailLabel.setVisible(!v);
    emailLabel.setManaged(!v);
    emailInput.setVisible(v);
    emailInput.setManaged(v);
    phoneLabel.setVisible(!v);
    phoneLabel.setManaged(!v);
    phoneInput.setVisible(v);
    phoneInput.setManaged(v);
    if (v) {
      User res = ClientSession.getCurrentUser();
      if (res != null) {
        fullNameInput.setText(res.getFullName() != null ? res.getFullName() : "");
        emailInput.setText(res.getEmail() != null ? res.getEmail() : "");
        phoneInput.setText(res.getPhoneNumber() != null ? res.getPhoneNumber() : "");
      }
    }
  }

  @FXML
  public void handleEditProfile() {
    if (editing) {
      String err =
          ClientSession.updateProfile(
              fullNameInput.getText(), emailInput.getText(), phoneInput.getText());
      if (err == null) {
        refreshData();
        KhungController.refreshSidebarFromSession();
      } else {
        new Alert(Alert.AlertType.ERROR, err).showAndWait();
      }
    }
    setEditingMode(!editing);
  }

  @FXML
  public void handleLogout() {
    ClientSession.clear();
    try {
      SceneManager.switchScene("/fxml/welcome.fxml");
    } catch (IOException e) {
    }
  }

  @FXML
  public void handleChangeAvatar() throws Exception {
    javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
    java.io.File res = fc.showOpenDialog(null);
    if (res != null) {
      byte[] ans = java.nio.file.Files.readAllBytes(res.toPath());
      String url = "https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload";
      String ans2 = NetworkClient.uploadFile(url, ans);
      ClientSession.updateAvatar(ans2);

      javafx.scene.image.Image ansimg = new javafx.scene.image.Image(ans2, true);
      ansimg
          .progressProperty()
          .addListener(
              (obs, oldv, newv) -> {
                if (newv.doubleValue() == 1.0 && !ansimg.isError()) {
                  double resw = ansimg.getWidth();
                  double resh = ansimg.getHeight();
                  double ansmin = Math.min(resw, resh);
                  double resx = (resw - ansmin) / 2;
                  double resy = (resh - ansmin) / 2;
                  Platform.runLater(
                      () -> {
                        avatarimageview.setImage(ansimg);
                        avatarimageview.setViewport(
                            new javafx.geometry.Rectangle2D(resx, resy, ansmin, ansmin));
                        avatarimageview.setFitWidth(68);
                        avatarimageview.setFitHeight(68);
                        avatarimageview.setPreserveRatio(false);
                        double ansr = 34.0;
                        avatarimageview.setClip(new javafx.scene.shape.Circle(ansr, ansr, ansr));
                      });
                }
              });
    }
  }

  public void handleRefresh(javafx.event.ActionEvent event) {
    if (ClientSession.getCurrentUser() == null) return;
    int res = ClientSession.getCurrentUser().getId();
    Request req = new Request("refresh_user", res);
    Response ans = NetworkClient.getInstance().sendRequestAndWait(req);
    if (ans != null && Response.OK.equals(ans.getStatus())) {
      ClientSession.setCurrentUser((User) ans.getPayload());
      Platform.runLater(this::refreshData);
    }
  }

  @FXML
  public void handleShowHistory() {
    try {
      javafx.fxml.FXMLLoader res =
          new javafx.fxml.FXMLLoader(
              getClass().getResource("/fxml/history/TransactionHistory.fxml"));
      javafx.scene.Parent ans = res.load();
      javafx.stage.Stage res1 = new javafx.stage.Stage();
      res1.setTitle("Lịch sử giao dịch");
      res1.setScene(new javafx.scene.Scene(ans, 800, 500));
      res1.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
