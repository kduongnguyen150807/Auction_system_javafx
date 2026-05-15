package com.auction.client.controller;

import com.auction.client.SceneManager;
import com.auction.client.network.NetworkClient;
import com.auction.shared.PasswordEncoder;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ForgotPasswordController {
    @FXML private VBox step1Box, step2Box;
    @FXML private TextField emailField, otpField;
    @FXML private PasswordField newPasswordField, confirmPasswordField;
    @FXML private Label messageLabel;

    private String currentEmail = "";

    @FXML
    public void handleSendOtp(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            messageLabel.setText("Vui lòng nhập email!");
            return;
        }

        messageLabel.setText("Đang gửi mã OTP...");
        step1Box.setDisable(true);

        // FIX: Lấy NetworkClient ở luồng chính (FX Thread) trước khi vào luồng phụ
        NetworkClient client;
        try {
            client = NetworkClient.getInstance();
        } catch (Exception e) {
            step1Box.setDisable(false);
            messageLabel.setText("Lỗi khởi tạo Network Client!");
            return;
        }

        new Thread(() -> {
            Request req = new Request(Request.FORGOT_PASSWORD_REQ, email);
            Response res = client.sendRequestAndWait(req);

            Platform.runLater(() -> {
                step1Box.setDisable(false);
                if (res != null && Response.OK.equals(res.getStatus())) {
                    currentEmail = email;
                    messageLabel.setText("Mã OTP đã được gửi tới email của bạn!");
                    step1Box.setVisible(false);
                    step1Box.setManaged(false);
                    step2Box.setVisible(true);
                    step2Box.setManaged(true);
                } else {
                    messageLabel.setText(res != null ? res.getMessage() : "Lỗi kết nối Server!");
                }
            });
        }).start();
    }

    @FXML
    public void handleResetPassword(ActionEvent event) {
        String otp = otpField.getText().trim();
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (otp.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Vui lòng nhập đủ thông tin!");
            return;
        }
        if (!pass.equals(confirm)) {
            messageLabel.setText("Mật khẩu không khớp!");
            return;
        }

        messageLabel.setText("Đang xử lý...");
        step2Box.setDisable(true);

        // FIX: Lấy NetworkClient ở luồng chính (FX Thread) trước khi vào luồng phụ
        NetworkClient client;
        try {
            client = NetworkClient.getInstance();
        } catch (Exception e) {
            step2Box.setDisable(false);
            messageLabel.setText("Lỗi khởi tạo Network Client!");
            return;
        }

        new Thread(() -> {
            Map<String, String> data = new HashMap<>();
            data.put("email", currentEmail);
            data.put("otp", otp);
            data.put("newPassword", PasswordEncoder.hash(pass)); // Hash SHA-256 tại Client

            Request req = new Request(Request.FORGOT_PASSWORD_RESET, data);
            Response res = client.sendRequestAndWait(req);

            Platform.runLater(() -> {
                step2Box.setDisable(false);
                if (res != null && Response.OK.equals(res.getStatus())) {
                    messageLabel.setStyle("-fx-text-fill: #44cc44;");
                    messageLabel.setText("Đổi mật khẩu thành công! Hãy đăng nhập lại.");
                    step2Box.setVisible(false);
                } else {
                    messageLabel.setText(res != null ? res.getMessage() : "Lỗi kết nối Server!");
                }
            });
        }).start();
    }

    @FXML
    public void backToLogin(ActionEvent event) throws Exception {
        SceneManager.switchScene("/fxml/login.fxml");
    }
}