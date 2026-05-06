package com.auction.client.ui.loginview;

import com.auction.client.ClientSession;
import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.base.PageController;
import com.auction.shared.link.Request;
import com.auction.shared.link.RequestType;
import com.auction.shared.link.Response;
import com.auction.shared.user.User;
import com.auction.shared.utils.PasswordEncoder;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý logic cho màn hình đăng nhập.
 *
 * <p>
 * Class này chịu trách nhiệm:
 * <ul>
 *   <li>Nhận input từ người dùng (username, password)</li>
 *   <li>Gửi request đăng nhập tới server</li>
 *   <li>Xử lý response và hiển thị thông báo</li>
 *   <li>Điều hướng về các view khác (ví dụ: quay lại Welcome)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Controller kế thừa từ {@link PageController}, cho phép sử dụng cơ chế
 * navigation thông qua {@code switchView}.
 * </p>
 */
public class LoginController extends PageController<LoginViewType> {
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private Label messageLabel;

  /**
   * Xử lý sự kiện khi người dùng nhấn nút "Back".
   *
   * <p>
   * Chuyển về màn hình {@link LoginViewType#WELCOME}.
   * </p>
   */
  @FXML
  private void back() {
    switchView.accept(LoginViewType.WELCOME);
  }

  /**
   * Xử lý sự kiện khi người dùng nhấn nút "Login".
   *
   * <p>
   * Quy trình:
   * <ol>
   *   <li>Đọc dữ liệu từ input</li>
   *   <li>Kiểm tra dữ liệu hợp lệ (không rỗng)</li>
   *   <li>Mã hóa mật khẩu</li>
   *   <li>Tạo {@link Request} và gửi tới server</li>
   *   <li>Nhận {@link Response} và xử lý kết quả</li>
   *   <li>Cập nhật thông báo cho người dùng</li>
   * </ol>
   * </p>
   *
   * <p>
   * Các trường hợp xử lý:
   * <ul>
   *   <li>Server không phản hồi → thông báo lỗi kết nối</li>
   *   <li>Đăng nhập thành công → thông báo thành công</li>
   *   <li>Tài khoản bị khóa → thông báo bị đình chỉ</li>
   *   <li>Sai thông tin → thông báo lỗi đăng nhập</li>
   * </ul>
   * </p>
   */
  @FXML
  private void handleLogin() {
    String username = this.usernameField.getText().trim();
    String rawPassword = this.passwordField.getText();
    if (username.isBlank() || rawPassword.isBlank()) {
      this.messageLabel.setText("Please enter username and password.");
      return;
    }

    String hashedPassword = PasswordEncoder.hash(rawPassword);
    Map<String, String> credentials = new HashMap<>();
    credentials.put("username", username);
    credentials.put("password", hashedPassword);

    Request request = new Request(RequestType.LOGIN, credentials);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);

    if (response == null) {
      this.messageLabel.setText("Cannot reach server — check IP and server status.");
      return;
    }

    if (response.getStatus().equals(Response.OK)) {
      if (response.getPayload() instanceof User loggedInUser) {
        ClientSession.setCurrentUser(loggedInUser);
      }
      this.messageLabel.setText("Login successful!");
      SceneManager.getInstance().switchTo(SceneType.HOME);
    } else if ("account_banned".equals(response.getMessage())) {
      this.messageLabel.setText("Your account has been suspended.");
    } else {
      this.messageLabel.setText("Incorrect username or password.");
    }
  }
}
