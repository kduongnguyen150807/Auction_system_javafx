package com.auction.client.ui.loginview.controller;

import com.auction.client.service.AuthService;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.loginview.LoginViewType;
import com.auction.shared.PasswordEncoder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

  public void setSwitchNode(Consumer<LoginViewType> switchNode) {
    this.switchNode = switchNode;
  }

  @FXML
  private void initialize() {
    Platform.runLater(() -> rootPane.requestFocus());
  }
  //Hàm xử lí login bên giao diện
  @FXML
  public void handleLogin(ActionEvent event) {
    Map<String,String> credentials = collectData();
    String loginMessage = AuthService.getInstance().login(credentials);
    setMessage(loginMessage);
  }

  private Map<String, String> collectData() {
    String username = usernameField.getText();
    String password = passwordField.getText();

    Map<String, String> credentials = new HashMap<>();
    credentials.put("username", username);
    credentials.put("password", PasswordEncoder.hash(password));

    return credentials;
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

  private void setMessage(String message) {
    this.messageLabel.setText(message);
  }
}
