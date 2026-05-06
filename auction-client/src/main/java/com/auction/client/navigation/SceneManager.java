package com.auction.client.navigation;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý việc điều hướng giữa các {@link Scene} trong ứng dụng JavaFX.
 *
 * <p>
 * Class này sử dụng Singleton pattern để đảm bảo chỉ có một instance duy nhất
 * quản lý toàn bộ các scene trong ứng dụng.
 * </p>
 *
 * <p>
 * Chức năng chính:
 * <ul>
 *   <li>Đăng ký các Scene với {@link SceneType}</li>
 *   <li>Chuyển đổi giữa các Scene</li>
 *   <li>Lấy root node của một Scene</li>
 * </ul>
 * </p>
 *
 * <p>
 * Lưu ý:
 * <ul>
 *   <li>Phải gọi {@link #setStage(Stage)} trước khi sử dụng</li>
 *   <li>Các Scene cần được đăng ký trước khi switch</li>
 * </ul>
 * </p>
 */
public class SceneManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SceneManager.class);
  /** Stage chính của ứng dụng */
  private Stage rootStage;
  /** Lưu trữ các Scene theo loại */
  private static final Map<SceneType, Scene> scenes = new HashMap<>();

  private static class Holder {
    private static final SceneManager INSTANCE = new SceneManager();
  }

  private SceneManager() {}

  public void setStage(Stage stage) {
    this.rootStage = stage;
  }

  public static SceneManager getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Đăng ký một Scene với tên tương ứng
   *
   * @param name  loại scene
   * @param scene đối tượng Scene cần đăng ký
   */
  public void registerScene(SceneType name, Scene scene) {
    scenes.put(name, scene);
  }

  /**
   * Chuyển sang Scene tương ứng
   *
   * @param name loại scene cần chuyển đến
   */
  public void switchTo(SceneType name) {
    Scene scene = scenes.get(name);

    if (scene == null) {
      LOGGER.error("Scene không tồn tại: {}", name);
    }

    rootStage.setScene(scene);
    rootStage.show();
  }
}
