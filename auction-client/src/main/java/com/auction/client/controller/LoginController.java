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

  @FXML
  public void handleLogin(ActionEvent event) {
    String username = this.usernameField.getText().trim();
    String rawPassword = this.passwordField.getText();

    if (username.isBlank() || rawPassword.isBlank()) {
      this.messageLabel.setText("Please enter username and password.");
      return;
    }

    // Hash before sending — plain-text never leaves this process.
    String hashedPassword = PasswordEncoder.hash(rawPassword);
    Map<String, String> credentials = new HashMap<>();
    credentials.put("username", username);
    credentials.put("password", hashedPassword);

    Request request = new Request(Request.LOGIN, credentials);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);

    if (response == null) {
      this.messageLabel.setText("Cannot reach server — check IP and server status.");
      return;
    }

    if (response.getStatus().equals(Response.OK)) {
      if (response.getPayload() instanceof User loggedInUser) {
        ClientSession.setCurrentUser(loggedInUser);
      }
      this.messageLabel.setText("Login successful!");
      try {
        SceneManager.switchScene("/fxml/main/Khung.fxml");
      } catch (Exception ex) {
        this.messageLabel.setText("Logged in but failed to open main window.");
      }
    } else if ("account_banned".equals(response.getMessage())) {
      this.messageLabel.setText("Your account has been suspended.");
    } else {
      this.messageLabel.setText("Incorrect username or password.");
    }
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
