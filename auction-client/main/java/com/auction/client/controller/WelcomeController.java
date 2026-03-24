package com.auction.client.controller;
import com.auction.client.SceneManager;
import javafx.event.ActionEvent;
public class WelcomeController {
    public void tologin(ActionEvent e) throws Exception {
        SceneManager.switchscene("/fxml/login.fxml");
    }
    public void toregister(ActionEvent e) throws Exception {
        SceneManager.switchscene("/fxml/register.fxml");
    }
}