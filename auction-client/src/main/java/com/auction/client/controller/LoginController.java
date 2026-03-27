package com.auction.client.controller;

import com.auction.client.ClientSession;
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

import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML private AnchorPane rootPane;
    @FXML private TextField u;
    @FXML private PasswordField p;
    @FXML private Label ans;

    @FXML
    private void initialize() {
        Platform.runLater(() -> rootPane.requestFocus());
    }

    // Đã sửa chữ 'L' viết hoa và thêm @FXML
    @FXML
    public void handleLogin(ActionEvent e) {
        String user = this.u.getText();
        String pass = this.p.getText();
        Map<String, String> data = new HashMap<>();
        data.put("username", user);
        data.put("password", pass);

        Request req = new Request(Request.login, data);
        NetworkClient.getinstance().sendrequest(req);

        Response res = NetworkClient.getinstance().receiveresponse();

        if (res != null && res.getstatus().equals(Response.ok)) {
            if (res.getpayload() instanceof User loggedInUser) {
                ClientSession.setCurrentUser(loggedInUser);
            }
            this.ans.setText("đăng nhập thành công!");
            try {
                SceneManager.switchscene("/fxml/main/Khung.fxml");
            } catch (Exception ex) {
                this.ans.setText("đăng nhập ok nhưng không mở được trang chủ");
            }
        } else {
            this.ans.setText("sai tài khoản hoặc mật khẩu!");
        }
    }

    // Thêm @FXML cho các hàm sự kiện khác để tránh lỗi tương tự
    @FXML
    public void back(ActionEvent e) throws Exception {
        SceneManager.switchscene("/fxml/welcome.fxml");
    }

    @FXML
    public void toregister(ActionEvent e) throws Exception {
        SceneManager.switchscene("/fxml/register.fxml");
    }
}