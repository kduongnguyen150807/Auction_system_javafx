package com.auction.client.ui.loginview.login;

import com.auction.client.ui.base.Validator;
import com.auction.client.ui.utils.ValidationResult;
import com.auction.shared.dto.LoginCredentials;

public class LoginCredentialsValidator extends Validator<LoginCredentials> {

  public ValidationResult validate(LoginCredentials credentials) {
    if (credentials == null) {
      return ValidationResult.fail("không nhận được gói tin tài ");
    } else if (credentials.getPassword().isBlank()) {
      return ValidationResult.fail("vui lòng nhập mật khẩu");
    } else if (credentials.getUsername().isBlank()) {
      return ValidationResult.fail("vui lòng nhập tài khoản");
    }

    return ValidationResult.ok();
  }

}
