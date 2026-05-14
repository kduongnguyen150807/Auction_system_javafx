package com.auction.client.ui.loginview.register;

import com.auction.client.ui.base.Validator;
import com.auction.client.ui.utils.ValidationResult;
import com.auction.shared.dto.RegisterCredentials;

public class RegisterCredentialsValidator extends Validator<RegisterCredentials> {
  @Override
  protected ValidationResult validate(RegisterCredentials credentials) {
    if (credentials.getUsername().isBlank() || credentials.getEmail().isBlank()
    || credentials.getPassword().isBlank() || credentials.getConfirmPassword().isBlank()
    || credentials.getAge() == -1) {
      return new ValidationResult(false, "Please fill all the fields");
    } else if (!credentials.getPassword().equals(credentials.getConfirmPassword())) {
      return new ValidationResult(false, "Passwords do not match");
    }

    return new ValidationResult(true, "");
  }
}
