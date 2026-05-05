package com.auction.client.ui.loginview;

import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.ui.PageController;
import javafx.fxml.FXML;
import javafx.scene.Scene;

import java.util.function.Consumer;

public class WelcomeController extends PageController {
  @FXML
  private void toLogin() {
    switchView.accept(LoginViewType.LOGIN);
  }

  @FXML
  private void toRegister() {

  }
}
