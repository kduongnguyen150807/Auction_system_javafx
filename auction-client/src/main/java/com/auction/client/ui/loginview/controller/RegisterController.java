package com.auction.client.ui.loginview.controller;

import com.auction.client.service.AuthService;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.loginview.LoginViewType;
import com.auction.client.util.InputValidators;
import com.auction.shared.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class RegisterController implements CanSwitchNode<LoginViewType> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

  private Consumer<LoginViewType> switchNode;

  public void setSwitchNode(Consumer<LoginViewType> switchNode) {
    this.switchNode = switchNode;
  }

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
    User newUser = collectData();
    if (newUser != null) {
      String message = AuthService.getInstance().register(newUser);
      setMessage(message);
    }
  }

  private User collectData() {
    String username = this.usernameField.getText();
    String email = this.emailField.getText();
    String age = this.ageField.getText();
    String password = this.passwordField.getText();
    String confirmPassword = this.confirmPasswordField.getText();

    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
      this.messageLabel.setText("nhập đủ thông tin vào!");
      return null;
    }

    if (!password.equals(confirmPassword)) {
      this.messageLabel.setText("mật khẩu không khớp!");
      return null;
    }

    if (!InputValidators.isValidEmail(email.trim())) {
      this.messageLabel.setText("email không hợp lệ!");
      return null;
    }

    Bidder newUser = new Bidder();
    newUser.setUsername(username);
    newUser.setEmail(email);
    newUser.setAge(age);
    newUser.setPassword(PasswordEncoder.hash(password));
    newUser.setFullName(username);
    return newUser;
  }

  private void setMessage(String message) {
    this.messageLabel.setText(message);
  }

  @FXML
  public void back(ActionEvent event) throws Exception {
    switchNode.accept(LoginViewType.LOGIN);
  }

  @FXML
  public void goWelcome(ActionEvent event) throws Exception {
    switchNode.accept(LoginViewType.WELCOME);
  }
}
