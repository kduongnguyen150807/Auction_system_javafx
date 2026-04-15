package com.auction.client;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    SceneManager.setStage(primaryStage);
    primaryStage.setTitle("hệ thống đấu giá online");
    SceneManager.switchScene("/fxml/welcome.fxml");
  }

  public static void main(String[] args) {
    launch(args);
  }
}
