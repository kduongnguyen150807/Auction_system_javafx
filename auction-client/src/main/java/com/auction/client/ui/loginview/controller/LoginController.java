package com.auction.client.ui.loginview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.user.AuthService;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.loginview.LoginViewType;
import com.auction.client.util.FXThread; // Đảm bảo dùng helper bọc Platform.runLater an toàn
import com.auction.shared.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LoginController implements CanSwitchNode<LoginViewType> {
  private Consumer<LoginViewType> switchNode;

  @FXML private AnchorPane rootPane;
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private Label messageLabel;
  @FXML private Button loginButton;

  private final AuthService authService;

  @AutoInject
  public LoginController(AuthService authService) {
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
  public void handleLogin(ActionEvent event) {
    Map<String, String> credentials = collectData();
    if (credentials == null) return;

    setLoadingState(true);
    setMessage("Đang xác thực thông tin...");

    authService.login(credentials)
      .thenAccept(response -> FXThread.run(() -> {
        setLoadingState(false);

        if (response != null && Response.OK.equals(response.getStatus())) {
          setMessage("Đăng nhập thành công! Đang chuyển hướng...");
          authService.switchHomeScene();
        } else {
          String errorMsg = (response != null) ? response.getMessage() : "Đăng nhập thất bại, máy chủ từ chối.";
          setMessage(errorMsg);
        }
      }))
      .exceptionally(ex -> {
        FXThread.run(() -> {
          setLoadingState(false);
          setMessage("Lỗi kết nối: Không thể kết nối tới máy chủ.");
        });
        return null;
      });
  }

  private Map<String, String> collectData() {
    String username = usernameField.getText().trim();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty()) {
      setMessage("Vui lòng nhập đủ tài khoản và mật khẩu!");
      return null;
    }

    Map<String, String> credentials = new HashMap<>();
    credentials.put("username", username);
    credentials.put("password", password);

    return credentials;
  }

  private void setLoadingState(boolean isLoading) {
    if (loginButton != null) {
      loginButton.setDisable(isLoading);
    }
    usernameField.setDisable(isLoading);
    passwordField.setDisable(isLoading);
  }

  private void setMessage(String message) {
    this.messageLabel.setText(message);
  }

  @FXML
  public void toForgotPassword(ActionEvent event) throws Exception {
    switchNode.accept(LoginViewType.FORGOT_PASSWORD);
  }

  @FXML
  public void back(ActionEvent event) throws Exception {
    switchNode.accept(LoginViewType.WELCOME);
  }

  @FXML
  public void toRegister(ActionEvent event) throws Exception {
    switchNode.accept(LoginViewType.REGISTER);
  }
}