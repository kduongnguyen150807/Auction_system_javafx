package com.auction.client.ui.loginview;

public enum LoginViewType {
  WELCOME("/fxml/LoginView/Welcome.fxml"),
  LOGIN("/fxml/LoginView/Login.fxml"),
  REGISTER("/fxml/LoginView/Register.fxml"),
  ;

  private final String fxmlPath;

  LoginViewType(String fxmlPath) {
    this.fxmlPath = fxmlPath;
  }

  public String getFxmlPath() {
    return fxmlPath;
  }
}
