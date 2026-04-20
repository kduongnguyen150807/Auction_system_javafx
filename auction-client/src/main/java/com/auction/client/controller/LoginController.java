package com.auction.client.controller;

import com.auction.client.ClientSession;
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

public class LoginController {

  @FXML private AnchorPane rootPane;
  @FXML private TextField u;
  @FXML private PasswordField p;
  @FXML private Label ans;

  @FXML
  private void initialize() {
    Platform.runLater(() -> rootPane.requestFocus());
  }

  @FXML
  public void handleLogin(ActionEvent event) {
    String username = this.u.getText();
    String password = this.p.getText();
    Map<String, String> credentials = new HashMap<>();
    credentials.put("username", username);
    credentials.put("password", password);

    Request request = new Request(Request.LOGIN, credentials);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);

    if (response != null && response.getStatus().equals(Response.OK)) {
      if (response.getPayload() instanceof User loggedInUser) {
        ClientSession.setCurrentUser(loggedInUser);
      }
      this.ans.setText("đăng nhập thành công!");
      try {
        SceneManager.switchScene("/fxml/main/Khung.fxml");
      } catch (Exception ex) {
        this.ans.setText("đăng nhập ok nhưng không mở được trang chủ");
      }
    } else {
      this.ans.setText("sai tài khoản hoặc mật khẩu!");
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
