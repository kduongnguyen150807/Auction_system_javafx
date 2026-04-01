package com.auction.client.ui.Profile;

import com.auction.client.ClientSession;
import com.auction.client.SceneManager;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.UserRole;
import java.io.IOException;
import javafx.fxml.FXML;
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
  @FXML private TextField fullNameInput;
  @FXML private TextField emailInput;
  @FXML private TextField phoneInput;
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
    if (ClientSession.getCurrentUser() != null) {
      String res = ClientSession.getCurrentUser().getavatarurl();
      if (res != null && !res.isEmpty()) {
        javafx.scene.image.Image ans = new javafx.scene.image.Image(res, true);
        avatarimageview.setImage(ans);
      }
    }
  }
}
