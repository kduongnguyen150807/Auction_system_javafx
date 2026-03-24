package com.auction.client.controller;
import com.auction.client.SceneManager;
import com.auction.client.network.NetworkClient;
import com.auction.shared.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.util.HashMap;
import java.util.Map;
public class LoginController {
    @FXML private TextField u;
    @FXML private PasswordField p;
    @FXML private Label ans;
    public void handlelogin(ActionEvent e) {
        String user = this.u.getText();
        String pass = this.p.getText();
        Map<String, String> data = new HashMap<>();
        data.put("username", user);
        data.put("password", pass);
        Request req = new Request(Request.login, data);
        NetworkClient.getinstance().sendrequest(req);
        Response res = NetworkClient.getinstance().receiveresponse();
        if (res != null && res.getstatus().equals(Response.ok)) {
            this.ans.setText("đăng nhập thành công!");
        } else {
            this.ans.setText("sai tài khoản hoặc mật khẩu!");
        }
    }
    public void back(ActionEvent e) throws Exception {
        SceneManager.switchscene("/fxml/welcome.fxml");
    }
    public void toregister(ActionEvent e) throws Exception {
        SceneManager.switchscene("/fxml/register.fxml");
    }
}