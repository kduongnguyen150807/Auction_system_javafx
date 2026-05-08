package com.auction.client.ui.loginview.login;

import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.service.AuthService;
import com.auction.client.ui.base.PageController;
import com.auction.client.ui.loginview.LoginViewType;
import com.auction.client.ui.utils.ValidationResult;
import com.auction.shared.dto.LoginCredentials;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;
import com.auction.shared.user.User;
import com.auction.shared.utils.PasswordEncoder;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


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
  private final LoginCredentialsValidator loginCredentialsValidator = new LoginCredentialsValidator();

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
    LoginCredentials credentials = collectData();
    ValidationResult validationResult = loginCredentialsValidator.validate(credentials);

    if (!validationResult.isValid()) {
      messageLabel.setText(validationResult.message());
      return;
    }

    AuthService.getInstance().login(credentials, this::onSendSuccess, this::onSendFailure);
  }

  private void onSendSuccess(Response<User> response) {
    messageLabel.setText(response.getMessage());
    SceneManager.getInstance().switchTo(SceneType.HOME);
  }

  private void onSendFailure(Throwable throwable) {
    messageLabel.setText(throwable.getMessage());
  }

  private LoginCredentials collectData() {
    String username = usernameField.getText();
    String password = passwordField.getText();

    String hashedPass = PasswordEncoder.hash(password);

    LoginCredentials credentials = new LoginCredentials(username, hashedPass);
    return credentials;
  }
}
