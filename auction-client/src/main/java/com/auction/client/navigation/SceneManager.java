package com.auction.client.navigation;

import com.auction.client.ui.homeview.HomeView;
import com.auction.client.ui.homeview.HomeViewType;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SceneManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SceneManager.class);

  private static SceneManager instance;

  public static SceneManager getInstance() {
    if (instance == null) {
      instance = new SceneManager();
    }
    return instance;
  }

  private static Map<SceneType, Scene> sceneMap = new HashMap<SceneType, Scene>();

  private Stage rootStage;

  public void setStage(Stage rootStage) {
    this.rootStage = rootStage;
  }

  public void registerScene(SceneType sceneType, Scene scene) {
    sceneMap.put(sceneType, scene);
  }

  public void switchScene(SceneType sceneType) {
    Scene scene = sceneMap.get(sceneType);
    if (scene != null) {
      rootStage.setScene(scene);
      rootStage.show();
    } else {
      LOGGER.info("scene {} does not exist!",  sceneType);
    }
  }

  public void buildHomeView() {
    Scene homeViewScene = new Scene(new HomeView());
    registerScene(SceneType.HOME_VIEW, homeViewScene);
  }
}
