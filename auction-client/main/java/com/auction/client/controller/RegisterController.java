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
public class RegisterController {
    @FXML private TextField u;
    @FXML private TextField e;
    @FXML private TextField a;
    @FXML private PasswordField p;
    @FXML private PasswordField cp;
    @FXML private Label ans;
    public void handleregister(ActionEvent ev) {
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
        Request req = new Request(Request.signup, data);
        NetworkClient.getinstance().sendrequest(req);
        Response res = NetworkClient.getinstance().receiveresponse();
        if (res != null && res.getstatus().equals(Response.ok)) {
            this.ans.setText("đăng ký thành công!");
        } else {
            this.ans.setText("đăng ký thất bại!");
        }
    }
    public void back(ActionEvent ev) throws Exception {
        SceneManager.switchscene("/fxml/login.fxml");
    }
}