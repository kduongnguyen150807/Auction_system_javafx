package com.auction.client.controller;

import com.auction.client.ClientSession;
import com.auction.client.SceneManager;
import com.auction.client.network.NetworkClient;
import com.auction.shared.*;
import com.auction.shared.PasswordEncoder;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class LoginController {

  @FXML private AnchorPane rootPane;
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private Label messageLabel;

  @FXML
  private void initialize() {
    Platform.runLater(() -> rootPane.requestFocus());
  }
  //Hàm xử lí login bên giao diện
  @FXML
  public void handleLogin(ActionEvent event) {
    String username = this.usernameField.getText().trim();
    String rawPassword = this.passwordField.getText();

    if (username.isBlank() || rawPassword.isBlank()) {
      this.messageLabel.setText("Please enter username and password.");
      return;
    }

    this.messageLabel.setText("Đang kết nối...");
    this.rootPane.setDisable(true);

    String hashedPassword = PasswordEncoder.hash(rawPassword);
    Map<String, String> credentials = new HashMap<>();
    credentials.put("username", username);
    credentials.put("password", hashedPassword);
    Request request = new Request(Request.LOGIN, credentials);

    NetworkClient client;
    try {
      client = NetworkClient.getInstance();
    } catch (Exception e) {
      this.rootPane.setDisable(false);
      this.messageLabel.setText("Cannot initialize network client.");
      return;
    }

    new Thread(() -> {
      Response res = client.sendRequestAndWait(request);

      Platform.runLater(() -> {
        this.rootPane.setDisable(false);

        if (res == null) {
          this.messageLabel.setText("Cannot reach server — check IP and server status.");
          return;
        }

        if (res.getStatus().equals(Response.OK)) {
          if (res.getPayload() instanceof User loggedInUser) {
            ClientSession.setCurrentUser(loggedInUser);
          }
          this.messageLabel.setText("Login successful!");
          try {
            SceneManager.switchScene("/fxml/main/Khung.fxml");
          } catch (Exception ex) {
            this.messageLabel.setText("Logged in but failed to open main window.");
          }
        } else if ("account_banned".equals(res.getMessage())) {
          this.messageLabel.setText("Your account has been suspended.");
        } else {
          this.messageLabel.setText("Incorrect username or password.");
        }
      });
    }).start();
  }
  @FXML
  public void toForgotPassword(ActionEvent event) throws Exception {
    SceneManager.switchScene("/fxml/forgot_password.fxml");
  }
  @FXML
  public void back(ActionEvent event) throws Exception {
    SceneManager.switchScene("/fxml/welcome.fxml");
  }

  @FXML
  public void toRegister(ActionEvent event) throws Exception {
    SceneManager.switchScene("/fxml/register.fxml");
  }
}
