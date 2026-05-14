package com.auction.client.ui.loginview.register;

import com.auction.client.service.AuthService;
import com.auction.client.ui.base.PageController;
import com.auction.client.ui.component.IntegerField;
import com.auction.client.ui.loginview.LoginView;
import com.auction.client.ui.loginview.LoginViewType;
import com.auction.client.ui.utils.ValidationResult;
import com.auction.shared.dto.RegisterCredentials;
import com.auction.shared.linkv2.ResponseStatus;
import com.auction.shared.utils.PasswordEncoder;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController extends PageController {
  private RegisterCredentialsValidator validator = new RegisterCredentialsValidator();

  @FXML private TextField usernameField;
  @FXML private TextField emailField;
  @FXML private PasswordField passwordField;
  @FXML private IntegerField ageField;
  @FXML private PasswordField confirmPasswordField;

  @FXML private Label messageLabel;


  @FXML
  private void handleRegister() {
    RegisterCredentials credentials = collectData();

    /* validate */
    ValidationResult validationResult = validator.validate(credentials);
    if(!validationResult.isValid()) {
      messageLabel.setText(validationResult.message());
      return;
    }

    AuthService.getInstance().register(credentials)
      .thenAccept(status -> {
        if (status == ResponseStatus.SUCCESS) {
          onSuccess();
        }
      });
  }

  private void onSuccess() {
    setMessage("register success");
    LoginView.switchNextScene();
  }

  private RegisterCredentials collectData() {
    return new  RegisterCredentials(
      usernameField.getText(),
      PasswordEncoder.hash(passwordField.getText()),
      PasswordEncoder.hash(confirmPasswordField.getText()),
      emailField.getText(),
      ageField.getValue()
    );
  }

  private void setMessage(String message) {
    messageLabel.setText(message);
  }

  @FXML
  private void goWelcome() {
    switchView.accept(LoginViewType.WELCOME);
  }

  @FXML
  private void back() {
    switchView.accept(LoginViewType.LOGIN);
  }
}
