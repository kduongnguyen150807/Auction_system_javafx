package com.auction.client.controller;

import com.auction.client.Main;
import com.auction.client.SceneManager;
import com.auction.client.Service.BackGroundService;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.auction.client.Service.BackGroundService.updateConfigUrl;

public class WelcomeController {

  @FXML
  private AnchorPane MainBackGround;

  private static AnchorPane Khung;

  @FXML
  public void initialize() throws IOException {
    Khung = MainBackGround;
    BackGroundService.apply(MainBackGround);
    NodeContentLoader<AnchorPane> Welcome2 = new NodeContentLoader<>();
    Welcome2.load("/fxml/Welcome2.fxml");

    NodeManager.addNodeToPane(Welcome2, MainBackGround);
  }

  public static AnchorPane getKhung() {
    return Khung;
  }
}