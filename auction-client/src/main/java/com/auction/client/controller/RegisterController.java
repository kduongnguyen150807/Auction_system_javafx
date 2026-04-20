package com.auction.client.controller;

import com.auction.client.SceneManager;
import com.auction.client.network.NetworkClient;
import com.auction.shared.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class RegisterController {
  @FXML private AnchorPane rootPane;
  @FXML private TextField u;
  @FXML private TextField e;
  @FXML private TextField a;
  @FXML private PasswordField p;
  @FXML private PasswordField cp;
  @FXML private Label ans;

  @FXML
  private void initialize() {
    Platform.runLater(() -> rootPane.requestFocus());
  }

  @FXML
  public void handleRegister(ActionEvent event) {
    String username = this.u.getText();
    String emailText = this.e.getText();
    String age = this.a.getText();
    String password = this.p.getText();
    String confirmPassword = this.cp.getText();

    if (username.isEmpty() || emailText.isEmpty() || password.isEmpty()) {
      this.ans.setText("nhập đủ thông tin vào!");
      return;
    }

    if (!password.equals(confirmPassword)) {
      this.ans.setText("mật khẩu không khớp!");
      return;
    }

    Bidder newUser = new Bidder();
    newUser.setUsername(username);
    newUser.setEmail(emailText);
    newUser.setAge(age);
    newUser.setPassword(password);
    newUser.setFullName(username);

    Request request = new Request(Request.SIGNUP, newUser);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);

    if (response != null && response.getStatus().equals(Response.OK)) {
      try {
        SceneManager.switchScene("/fxml/login.fxml");
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } else {
      String message = (response != null) ? response.getMessage() : "timeout";
      if ("duplicate_username_or_email".equals(message)) {
        this.ans.setText("username hoặc email đã tồn tại!");
      } else {
        this.ans.setText("đăng ký thất bại!");
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
