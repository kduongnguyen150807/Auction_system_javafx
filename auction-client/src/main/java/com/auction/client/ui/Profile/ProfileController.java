package com.auction.client.ui.Profile;

import com.auction.client.ClientSession;
import com.auction.client.SceneManager;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.*;
import com.auction.client.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileController {
  @FXML private javafx.scene.image.ImageView avatarimageview;
  @FXML private Label usernameLabel, fullNameLabel, emailLabel, phoneLabel, roleLabel, balanceLabel;
  @FXML private Label moneySpentLabel, itemsBoughtLabel, moneyReceivedLabel, itemsSoldLabel;
  @FXML private TextField fullNameInput, emailInput, phoneInput, DepositAmountField;
  @FXML private Button editButton, toggleRoleButton;
  @FXML private HBox bidderMetricsRow, sellerMetricsRow;

  private boolean editing = false;
  private static ProfileController instance;

  public static ProfileController getinstance() { return instance; }

  @FXML
  public void initialize() {
    instance = this;
    refreshData();
    setEditingMode(false);
  }

  public void updatebalancedirectly(User u) {
    Platform.runLater(() -> {
      ClientSession.setCurrentUser(u);
      refreshData();
    });
  }

  @FXML
  public void handleDeposit() {
    try {
      double res = Double.parseDouble(DepositAmountField.getText());
      Map<String, String> ans = new HashMap<>();
      ans.put("userid", String.valueOf(ClientSession.getCurrentUser().getid()));
      ans.put("amount", String.valueOf(res));
      Request req = new Request("deposit", (java.io.Serializable) ans);
      Response ans2 = NetworkClient.getinstance().sendrequestandwait(req);
      if (ans2 != null && Response.ok.equals(ans2.getstatus())) {
        ClientSession.setCurrentUser((User) ans2.getpayload());
        refreshData();
      }
    } catch (Exception e) {}
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
      usernameLabel.setText(res.getusername());
      fullNameLabel.setText(res.getfullname());
      emailLabel.setText(res.getemail());
      phoneLabel.setText(res.getphonenumber());
      roleLabel.setText(ans.name());
      if (balanceLabel != null) balanceLabel.setText(String.format("%,.0f$", res.getbalance()));
      if (moneySpentLabel != null) moneySpentLabel.setText(String.format("%,.0f$", res.getmoneyspent()));
      if (itemsBoughtLabel != null) itemsBoughtLabel.setText(String.valueOf(res.getitemsbought()));
      if (moneyReceivedLabel != null) moneyReceivedLabel.setText(String.format("%,.0f$", res.getmoneyreceived()));
      if (itemsSoldLabel != null) itemsSoldLabel.setText(String.valueOf(res.getitemssold()));

      toggleRoleButton.setVisible(res.getrole() != UserRole.ADMIN);
      toggleRoleButton.setText(ans == UserRole.SELLER ? "Switch to Bidder" : "Switch to Seller");

      boolean res2 = ans == UserRole.BIDDER;
      if (bidderMetricsRow != null) { bidderMetricsRow.setVisible(res2); bidderMetricsRow.setManaged(res2); }
      if (sellerMetricsRow != null) { sellerMetricsRow.setVisible(!res2); sellerMetricsRow.setManaged(!res2); }

      if (res.getavatarurl() != null && !res.getavatarurl().isEmpty()) {
        javafx.scene.image.Image ansimg = new javafx.scene.image.Image(res.getavatarurl(), true);
        ansimg.progressProperty().addListener((obs, oldv, newv) -> {
          if (newv.doubleValue() == 1.0 && !ansimg.isError()) {
            double resw = ansimg.getWidth();
            double resh = ansimg.getHeight();
            double ansmin = Math.min(resw, resh);
            double resx = (resw - ansmin) / 2;
            double resy = (resh - ansmin) / 2;
            Platform.runLater(() -> {
              avatarimageview.setImage(ansimg);
              avatarimageview.setViewport(new javafx.geometry.Rectangle2D(resx, resy, ansmin, ansmin));
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
    fullNameLabel.setVisible(!v); fullNameInput.setVisible(v);
    emailLabel.setVisible(!v); emailInput.setVisible(v);
    phoneLabel.setVisible(!v); phoneInput.setVisible(v);
  }

  @FXML public void handleEditProfile() { setEditingMode(!editing); }
  @FXML public void handleLogout() { ClientSession.clear(); try { SceneManager.switchscene("/fxml/welcome.fxml"); } catch (IOException e) {} }

  @FXML
  public void handlechangeavatar() throws Exception {
    javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
    java.io.File res = fc.showOpenDialog(null);
    if (res != null) {
      byte[] ans = java.nio.file.Files.readAllBytes(res.toPath());
      String url = "https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload";
      String ans2 = NetworkClient.uploadfile(url, ans);
      ClientSession.updateavatar(ans2);

      javafx.scene.image.Image ansimg = new javafx.scene.image.Image(ans2, true);
      ansimg.progressProperty().addListener((obs, oldv, newv) -> {
        if (newv.doubleValue() == 1.0 && !ansimg.isError()) {
          double resw = ansimg.getWidth();
          double resh = ansimg.getHeight();
          double ansmin = Math.min(resw, resh);
          double resx = (resw - ansmin) / 2;
          double resy = (resh - ansmin) / 2;
          Platform.runLater(() -> {
            avatarimageview.setImage(ansimg);
            avatarimageview.setViewport(new javafx.geometry.Rectangle2D(resx, resy, ansmin, ansmin));
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

  public void handlerefresh(javafx.event.ActionEvent event) {
    if (ClientSession.getCurrentUser() == null) return;
    int res = ClientSession.getCurrentUser().getid();
    Request req = new Request("refresh_user", res);
    Response ans = NetworkClient.getinstance().sendrequestandwait(req);
    if (ans != null && Response.ok.equals(ans.getstatus())) {
      ClientSession.setCurrentUser((User) ans.getpayload());
      Platform.runLater(this::refreshData);
    }
  }
  @FXML
  public void handleShowHistory() {
    try {
      javafx.fxml.FXMLLoader res = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/history/TransactionHistory.fxml"));
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