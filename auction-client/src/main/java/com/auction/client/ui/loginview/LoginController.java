package com.auction.client.ui.loginview;

import com.auction.client.network.NetworkClient;
import com.auction.client.ui.PageController;
import com.auction.shared.link.Request;
import com.auction.shared.link.Response;
import com.auction.shared.utils.PasswordEncoder;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LoginController extends PageController {
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private Label messageLabel;

  @FXML
  private void back() {
    switchView.accept(LoginViewType.WELCOME);
  }

  @FXML
  private void handleLogin() {
    String username = this.usernameField.getText().trim();
    String rawPassword = this.passwordField.getText();
    if (username.isBlank() || rawPassword.isBlank()) {
      this.messageLabel.setText("Please enter username and password.");
      return;
    }

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
      messageLabel.setText("Logged in successfully.");
    } else if ("account_banned".equals(response.getMessage())) {
      this.messageLabel.setText("Your account has been suspended.");
    } else {
      this.messageLabel.setText("Incorrect username or password.");
    }

  }
}
