package com.auction.client.ui.loginview;

import com.auction.client.network.NetworkClient;
import com.auction.client.ui.base.PageController;
import javafx.fxml.FXML;

public class WelcomeController extends PageController<LoginViewType> {
  @FXML
  private void toLogin() {
    switchView.accept(LoginViewType.LOGIN);
  }

  @FXML
  private void toRegister() {
    switchView.accept(LoginViewType.REGISTER);
  }
}
