package com.auction.client.ui.loginview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.user.AuthService;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.loginview.LoginViewType;
import com.auction.client.util.FXThread; // Sử dụng helper bọc Platform.runLater an toàn
import com.auction.client.util.InputValidators;
import com.auction.shared.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class RegisterController implements CanSwitchNode<LoginViewType> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

  private final AuthService authService;
  private Consumer<LoginViewType> switchNode;

  @FXML private AnchorPane rootPane;
  @FXML private TextField usernameField;
  @FXML private TextField emailField;
  @FXML private TextField ageField;
  @FXML private PasswordField passwordField;
  @FXML private PasswordField confirmPasswordField;
  @FXML private Label messageLabel;
  @FXML private Button registerButton;

  @AutoInject
  public RegisterController(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public void setSwitchNode(Consumer<LoginViewType> switchNode) {
    this.switchNode = switchNode;
  }

  @FXML
  private void initialize() {
    Platform.runLater(() -> rootPane.requestFocus());
  }

  @FXML
  public void handleRegister(ActionEvent event) {
    User newUser = collectData();
    if (newUser == null) return;

    setLoadingState(true);
    setMessage("Đang xử lý đăng ký...");

    authService.register(newUser)
      .thenAccept(response -> FXThread.run(() -> {
        setLoadingState(false);

        if (response != null && Response.OK.equals(response.getStatus())) {
          setMessage("Đăng ký thành công! Đang chuyển hướng...");
          authService.switchHomeScene();
        } else {
          String errorMsg = (response != null) ? response.getMessage() : "Đăng ký thất bại, máy chủ từ chối.";
          setMessage(errorMsg);
        }
      }))
      .exceptionally(ex -> {
        FXThread.run(() -> {
          setLoadingState(false);
          setMessage("Lỗi kết nối: Không thể gửi yêu cầu đến máy chủ.");
          LOGGER.error("Xảy ra lỗi trong quá trình kết nối đăng ký", ex);
        });
        return null;
      });
  }

  private User collectData() {
    String username = this.usernameField.getText().trim();
    String email = this.emailField.getText().trim();
    String age = this.ageField.getText().trim();
    String password = this.passwordField.getText();
    String confirmPassword = this.confirmPasswordField.getText();

    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
      setMessage("Nhập đủ thông tin vào!");
      return null;
    }

    if (!password.equals(confirmPassword)) {
      setMessage("Mật khẩu không khớp!");
      return null;
    }

    if (!InputValidators.isValidEmail(email)) {
      setMessage("Email không hợp lệ!");
      return null;
    }

    Bidder newUser = new Bidder();
    newUser.setUsername(username);
    newUser.setEmail(email);
    newUser.setAge(age);
    newUser.setPassword(password);
    newUser.setFullName(username);
    return newUser;
  }

  private void setMessage(String message) {
    this.messageLabel.setText(message);
  }

  private void setLoadingState(boolean isLoading) {
    if (registerButton != null) {
      registerButton.setDisable(isLoading);
    }
    usernameField.setDisable(isLoading);
    emailField.setDisable(isLoading);
    ageField.setDisable(isLoading);
    passwordField.setDisable(isLoading);
    confirmPasswordField.setDisable(isLoading);
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