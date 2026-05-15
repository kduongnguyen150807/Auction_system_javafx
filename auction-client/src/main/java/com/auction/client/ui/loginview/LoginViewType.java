package com.auction.client.ui.loginview;

public enum LoginViewType {
  WELCOME("/fxml/loginview/welcome.fxml"),
  LOGIN("/fxml/loginview/login.fxml"),
  REGISTER("/fxml/loginview/register.fxml"),
  FORGOT_PASSWORD("/fxml/loginview/forgot_password.fxml"),
  ;

  private final String fxmlPath;

  LoginViewType(String fxmlPath) {
    this.fxmlPath = fxmlPath;
  }

  public String getFxmlPath() {
    return fxmlPath;
  }
}
