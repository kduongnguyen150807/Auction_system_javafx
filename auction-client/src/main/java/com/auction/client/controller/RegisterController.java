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
  public void handleRegister(ActionEvent ev) {
    String u1 = this.u.getText();
    String e1 = this.e.getText();
    String a1 = this.a.getText();
    String p1 = this.p.getText();
    String p2 = this.cp.getText();

    if (u1.isEmpty() || e1.isEmpty() || p1.isEmpty()) {
      this.ans.setText("nhập đủ thông tin vào!");
      return;
    }

    if (!p1.equals(p2)) {
      this.ans.setText("mật khẩu không khớp!");
      return;
    }

    Bidder res = new Bidder();
    res.setUsername(u1);
    res.setEmail(e1);
    res.setAge(a1);
    res.setPassword(p1);
    res.setFullName(u1);

    Request req = new Request(Request.SIGNUP, res);
    Response ans_res = NetworkClient.getInstance().sendRequestAndWait(req);

    if (ans_res != null && ans_res.getStatus().equals(Response.OK)) {
      try {
        SceneManager.switchScene("/fxml/login.fxml");
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } else {
      String msg = (ans_res != null) ? ans_res.getMessage() : "timeout";
      if ("duplicate_username_or_email".equals(msg)) {
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