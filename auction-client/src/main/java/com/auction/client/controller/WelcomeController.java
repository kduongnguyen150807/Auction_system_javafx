package com.auction.client.controller;

import com.auction.client.SceneManager;
import javafx.event.ActionEvent;

public class WelcomeController {
  public void toLogin(ActionEvent e) throws Exception {
    SceneManager.switchScene("/fxml/login.fxml");
  }

  public void toRegister(ActionEvent e) throws Exception {
    SceneManager.switchScene("/fxml/register.fxml");
  }
}
