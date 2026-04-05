package com.auction.client.controller;

import com.auction.client.SceneManager;
import com.auction.client.network.NetworkClient;
import com.auction.shared.*;
import java.util.HashMap;
import java.util.Map;
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
  public void handleRegister(ActionEvent ev) {
    String user = this.u.getText();
    String email = this.e.getText();
    String age = this.a.getText();
    String pass = this.p.getText();
    String cpass = this.cp.getText();
    if (!pass.equals(cpass)) {
      this.ans.setText("mật khẩu không khớp!");
      return;
    }
    Map<String, String> data = new HashMap<>();
    data.put("username", user);
    data.put("email", email);
    data.put("age", age);
    data.put("password", pass);
    Request req = new Request(Request.SIGNUP, data);
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && res.getStatus().equals(Response.OK)) {
      this.ans.setText("đăng ký thành công!");
    } else {
      if (res != null && "duplicate_username_or_email".equals(res.getMessage())) {
        this.ans.setText("username hoặc email đã tồn tại!");
      } else {
        this.ans.setText("đăng ký thất bại!");
      }
    }
  }

  @FXML
  public void back(ActionEvent ev) throws Exception {
    SceneManager.switchScene("/fxml/login.fxml");
  }

  @FXML
  public void goWelcome(ActionEvent ev) throws Exception {
    SceneManager.switchScene("/fxml/welcome.fxml");
  }
}
