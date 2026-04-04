package com.auction.client.ui.Profile;

import com.auction.client.ClientSession;
import com.auction.client.SceneManager;
import com.auction.client.controller.AuthController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.UserRole;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.client.network.NetworkClient;
import com.auction.shared.User;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ProfileController {
  @FXML private javafx.scene.image.ImageView avatarimageview;
  @FXML private Label usernameLabel;
  @FXML private Label fullNameLabel;
  @FXML private Label emailLabel;
  @FXML private Label phoneLabel;
  @FXML private Label roleLabel;
  @FXML private Label balanceLabel;
  @FXML private Label moneySpentLabel;
  @FXML private Label itemsBoughtLabel;
  @FXML private Label moneyReceivedLabel;
  @FXML private Label itemsSoldLabel;
  @FXML private TextField fullNameInput;
  @FXML private TextField emailInput;
  @FXML private TextField phoneInput;
  @FXML private TextField DepositAmountField;
  @FXML private Button editButton;
  @FXML private Button toggleRoleButton;
  @FXML private HBox bidderMetricsRow;
  @FXML private HBox sellerMetricsRow;

  private boolean editing = false;

  @FXML
  public void initialize() {
    refreshData();
    setEditingMode(false);
  }

  @FXML
  public void handleDeposit() {
    try {
      double val = Double.parseDouble(DepositAmountField.getText());
      Map<String, String> res = new HashMap<>();
      res.put("userid", String.valueOf(ClientSession.getCurrentUser().getid()));
      res.put("amount", String.valueOf(val));

      Request req = new Request("deposit", (java.io.Serializable) res);
      Response ans = NetworkClient.getinstance().sendrequestandwait(req);

      if (ans != null && Response.ok.equals(ans.getstatus())) {
        User u = (User) ans.getpayload();
        ClientSession.setCurrentUser(u);
        if (balanceLabel != null) balanceLabel.setText(String.format("%,.0f$", u.getbalance()));
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Nạp tiền thành công");
        a.setHeaderText(null);
        a.setContentText("Mày đã nạp vào tài khoản " + val + "$!");
        a.showAndWait();
      }
    } catch (Exception e) {}
  }

  @FXML
  public void handleEditProfile() {
    if (!editing) {
      setEditingMode(true);
      return;
    }
    String newEmail = emailInput.getText();
    if (!AuthController.isValidEmail(newEmail)) {
      Alert a = new Alert(Alert.AlertType.WARNING);
      a.setTitle("Cannot save profile");
      a.setHeaderText(null);
      a.setContentText("Định dạng email không hợp lệ");
      a.showAndWait();
      emailInput.setText(fallback(ClientSession.getEmail(), "username@mail.com"));
      return;
    }
    String err =
            ClientSession.updateProfile(
                    fullNameInput.getText(), newEmail, phoneInput.getText());
    if (err != null) {
      Alert a = new Alert(Alert.AlertType.WARNING);
      a.setTitle("Cannot save profile");
      a.setHeaderText(null);
      a.setContentText(profileErrorMessage(err));
      a.showAndWait();
      if ("duplicate_email".equals(err) || "duplicate_phone".equals(err)) {
        emailInput.setText(fallback(ClientSession.getEmail(), "username@mail.com"));
        phoneInput.setText(fallback(ClientSession.getPhone(), "N/A"));
      }
      return;
    }
    refreshData();
    setEditingMode(false);
    KhungController.refreshSidebarFromSession();
  }

  private String profileErrorMessage(String code) {
    if (code == null) return "";
    return switch (code) {
      case "duplicate_email" -> "This email is already used by another account.";
      case "duplicate_phone" -> "This phone number is already used by another account.";
      case "invalid_email" -> "Email cannot be empty.";
      case "update_failed" -> "Could not update profile. Please try again.";
      case "not_logged_in" -> "You are not logged in.";
      default -> "Could not save profile.";
    };
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
    String fullname = fallback(ClientSession.getFullName(), username);
    String email = fallback(ClientSession.getEmail(), "username@mail.com");
    String phone = fallback(ClientSession.getPhone(), "N/A");
    UserRole role = ClientSession.getActiveRole();
    usernameLabel.setText(username);
    fullNameLabel.setText(fullname);
    emailLabel.setText(email);
    phoneLabel.setText(phone);
    roleLabel.setText(toTitle(role.name()));
    fullNameInput.setText(fullname);
    emailInput.setText(email);
    phoneInput.setText(phone);
    UserRole ans = role;
    boolean res = ans == UserRole.ADMIN;
    toggleRoleButton.setVisible(!res);
    toggleRoleButton.setManaged(!res);
    if (!res) toggleRoleButton.setText(ans == UserRole.SELLER ? "Switch to Bidder" : "Switch to Seller");
    if (ClientSession.getCurrentUser() != null) {
      if (balanceLabel != null) {
        balanceLabel.setText(String.format("%,.0f$", ClientSession.getCurrentUser().getbalance()));
      }
      if (moneySpentLabel != null) {
        moneySpentLabel.setText(String.format("%,.0f$", ClientSession.getCurrentUser().getmoneyspent()));
      }
      if (itemsBoughtLabel != null) {
        itemsBoughtLabel.setText(String.valueOf(ClientSession.getCurrentUser().getitemsbought()));
      }
      if (moneyReceivedLabel != null) {
        moneyReceivedLabel.setText(String.format("%,.0f$", ClientSession.getCurrentUser().getmoneyreceived()));
      }
      if (itemsSoldLabel != null) {
        itemsSoldLabel.setText(String.valueOf(ClientSession.getCurrentUser().getitemssold()));
      }
      {
        String avatarurl = ClientSession.getCurrentUser().getavatarurl();
        if (avatarurl != null && !avatarurl.isEmpty()) {
          javafx.application.Platform.runLater(() -> {
            javafx.scene.image.Image img = new javafx.scene.image.Image(avatarurl, true);
            avatarimageview.setImage(img);
          });
        }
      }
      {
        double radius = 34.0;
        javafx.application.Platform.runLater(() -> {
          avatarimageview.setClip(new javafx.scene.shape.Circle(radius, radius, radius));
        });
      }
    }
    KhungController.refreshSidebarFromSession();
    applyRoleMetricsVisibility(role);
  }

  private void applyRoleMetricsVisibility(UserRole activeRole) {
    if (bidderMetricsRow == null || sellerMetricsRow == null) {
      return;
    }
    boolean showBidder = activeRole == UserRole.BIDDER;
    boolean showSeller = activeRole == UserRole.SELLER;
    bidderMetricsRow.setVisible(showBidder);
    bidderMetricsRow.setManaged(showBidder);
    sellerMetricsRow.setVisible(showSeller);
    sellerMetricsRow.setManaged(showSeller);
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

  public void handlechangeavatar(javafx.event.ActionEvent event) throws Exception {
    javafx.stage.FileChooser filechooser = new javafx.stage.FileChooser();
    java.io.File file = filechooser.showOpenDialog(null);
    if (file != null) {
      java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
      String boundary = "boundary123";
      String head = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"avatar.png\"\r\n\r\n";
      String tail = "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\nupload_def\r\n--" + boundary + "--\r\n";
      byte[] headbytes = head.getBytes();
      byte[] filebytes = java.nio.file.Files.readAllBytes(file.toPath());
      byte[] tailbytes = tail.getBytes();
      byte[] body = new byte[headbytes.length + filebytes.length + tailbytes.length];
      System.arraycopy(headbytes, 0, body, 0, headbytes.length);
      System.arraycopy(filebytes, 0, body, headbytes.length, filebytes.length);
      System.arraycopy(tailbytes, 0, body, headbytes.length + filebytes.length, tailbytes.length);
      java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
              .uri(java.net.URI.create("https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload"))
              .header("Content-Type", "multipart/form-data; boundary=" + boundary)
              .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(body))
              .build();
      java.net.http.HttpResponse<String> res = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
      String ans = res.body().split("\"secure_url\":\"")[1].split("\"")[0];
      avatarimageview.setImage(new javafx.scene.image.Image(ans));
      ClientSession.updateavatar(ans);
    }
  }

  public void handlerefresh(javafx.event.ActionEvent event) {
    if (ClientSession.getCurrentUser() == null) return;
    try {
      int res = ClientSession.getCurrentUser().getid();
      Request req = new Request("refresh_user", res);
      Response ans = NetworkClient.getinstance().sendrequestandwait(req);

      if (ans != null && Response.ok.equals(ans.getstatus())) {
        User u = (User) ans.getpayload();
        ClientSession.setCurrentUser(u);
        javafx.application.Platform.runLater(() -> {
          refreshData();
        });
      }
    } catch (Exception e) {}
  }
}