package com.auction.client;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
  private static Stage rootStage;
  private static final String GLOBAL_STYLE = "/css/GlobalColor.css";

  public static void setStage(Stage stage) {
    rootStage = stage;
  }

  public static void switchScene(String fxmlPath) throws IOException {
    Parent res = FXMLLoader.load(Objects.requireNonNull(SceneManager.class.getResource(fxmlPath)));
    Scene currentScene = rootStage.getScene();
    if (currentScene == null) {
      rootStage.setScene(new Scene(res));
    } else {
      currentScene.setRoot(res);
    }
    URL styleUrl = SceneManager.class.getResource(GLOBAL_STYLE);
    if (styleUrl != null) {
      String style = styleUrl.toExternalForm();
      if (!rootStage.getScene().getStylesheets().contains(style)) {
        rootStage.getScene().getStylesheets().add(style);
      }
    }
    rootStage.show();
  }
}
