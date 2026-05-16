package com.auction.client;

import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.network.NetworkClient;
import com.auction.client.network.NotificationDispatcher;
import com.auction.client.ui.homeview.HomeView;
import com.auction.client.ui.loginview.LoginView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppBootstrap {
  public static void run(Stage stage) {
    SceneManager sceneManager = SceneManager.getInstance();
    sceneManager.setStage(stage);

    /* khoi tao scene */
    Scene loginViewScene = new Scene(new LoginView());

    /* register scene */
    sceneManager.registerScene(SceneType.LOGIN_VIEW, loginViewScene);

    sceneManager.switchScene(SceneType.LOGIN_VIEW);

    /* khoi tao NotificationDispatcher */
    NotificationDispatcher notificationDispatcher = new NotificationDispatcher();

    /* add NotificationDispatcher */
    NetworkClient.getInstance().addListener(notificationDispatcher);
  }
}
