package com.auction.client;

import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.loginview.LoginView;
import com.auction.client.ui.maindashboard.HomeView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Điểm khởi chạy chính của ứng dụng đấu giá (EntryPoint).
 * Lớp này chịu trách nhiệm khởi tạo cửa sổ chính (Stage), thiết lập SceneManager
 * và nạp các phân đoạn giao diện lớn của hệ thống.
 *
 */
public class Main extends Application {
  private SceneManager sceneManager;

  /**
   * Phương thức khởi tạo giao diện chính của JavaFX.
   *
   * @param stage Sân khấu chính (Primary Stage) do JavaFX Runtime cung cấp.
   */
  @Override
  public void start(Stage stage) throws Exception {
    sceneManager = SceneManager.getInstance();
    sceneManager.setStage(stage);
    stage.setTitle("hệ thống đấu giá online");
    boolean isConnected = NetworkClient.getInstance().connect();
    if (isConnected) {
      buildScene();
      sceneManager.switchTo(SceneType.LOGIN);
    } else {
      System.out.println("Ứng dụng không thể tiếp tục nếu không có kết nối.");
      Platform.exit();
    }
  }

  /**
   * Khởi tạo các Scene và đăng ký chúng vào SceneManager.
   * Ở đây, LoginView được bọc trong một Scene để làm điểm bắt đầu.
   *
   */
  private void buildScene() {
    Scene LoginView = new Scene(new LoginView());
    sceneManager.registerScene(SceneType.LOGIN ,LoginView);

    Scene Home = new Scene(new HomeView());
    sceneManager.registerScene(SceneType.HOME ,Home);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
