package com.auction.client.controller;

import com.auction.client.SceneManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.util.InputValidators;
import com.auction.shared.*;
import com.auction.shared.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class RegisterController {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

  @FXML private AnchorPane rootPane;
  @FXML private TextField usernameField;
  @FXML private TextField emailField;
  @FXML private TextField ageField;
  @FXML private PasswordField passwordField;
  @FXML private PasswordField confirmPasswordField;
  @FXML private Label messageLabel;

  @FXML
  private void initialize() {
    Platform.runLater(() -> rootPane.requestFocus());
  }

  @FXML
  public void handleRegister(ActionEvent event) {
    String username = this.usernameField.getText();
    String email = this.emailField.getText();
    String age = this.ageField.getText();
    String password = this.passwordField.getText();
    String confirmPassword = this.confirmPasswordField.getText();

    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
      this.messageLabel.setText("nhập đủ thông tin vào!");
      return;
    }

    if (!password.equals(confirmPassword)) {
      this.messageLabel.setText("mật khẩu không khớp!");
      return;
    }

    if (!InputValidators.isValidEmail(email.trim())) {
      this.messageLabel.setText("email không hợp lệ!");
      return;
    }

    Bidder newUser = new Bidder();
    newUser.setUsername(username);
    newUser.setEmail(email);
    newUser.setAge(age);
    newUser.setPassword(PasswordEncoder.hash(password));
    newUser.setFullName(username);

    Request request = new Request(Request.SIGNUP, newUser);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);

    if (response != null && response.getStatus().equals(Response.OK)) {
      try {
        SceneManager.switchScene("/fxml/login.fxml");
      } catch (Exception ex) {
        LOGGER.error("Navigation to login after register failed", ex);
      }
    } else {
      String message = (response != null) ? response.getMessage() : "timeout";
      if ("duplicate_username_or_email".equals(message)) {
        this.messageLabel.setText("username hoặc email đã tồn tại!");
      } else {
        this.messageLabel.setText("đăng ký thất bại!");
      }
    }
  }

  @FXML
  public void back(ActionEvent event) throws Exception {
    SceneManager.switchScene("/fxml/login.fxml");
  }

  @FXML
  public void goWelcome(ActionEvent event) throws Exception {
    SceneManager.switchScene("/fxml/welcome.fxml");
  }
}
