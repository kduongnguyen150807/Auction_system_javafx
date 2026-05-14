package com.auction.client.ui.loginview;

import com.auction.client.app.NodeLoader;
import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.ui.base.PageController;
import com.auction.client.ui.maindashboard.HomeView;
import com.auction.client.ui.utils.FXThread;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp điều khiển chính cho giao diện đăng nhập (LoginView).
 * Sử dụng cấu trúc {@link StackPane} để quản lý và chuyển đổi giữa các màn hình con
 * như Welcome, Login, và Register.
 *
 */
public class LoginView extends StackPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoginView.class);

  private static final String BASE_FXML_PATH = "/fxml/LoginView/LoginView.fxml";
  private static final String BASE_STYLESHEET_PATH = "/css/LoginView/LoginView.css";
  /**
   * Lưu trữ các instance của Node tương ứng với từng loại màn hình
   */
  private final Map<LoginViewType, Node> nodeMap = new EnumMap<>(LoginViewType.class);

  /**
   * Khởi tạo LoginView, nạp file FXML gốc và thiết lập các thành phần con.
   */
  public LoginView() {
    initBaseLayout();
    initNodes();
    switchNode(LoginViewType.WELCOME);
  }

  /**
   * Nạp layout gốc từ FXML và thiết lập CSS cho component.
   */
  private void initBaseLayout() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    loader.setRoot(this);
    loader.setController(this);

    String cssPath = getClass().getResource(BASE_STYLESHEET_PATH).toExternalForm();
    getStylesheets().add(cssPath);

    try {
      loader.load();
      LOGGER.info("LoginView base layout loaded successfully.");
    } catch (IOException e) {
      LOGGER.error("Failed to load LoginView.fxml", e);
      throw new RuntimeException("Critical UI load failure", e);
    }
  }

  /**
   * Khởi tạo các màn hình con và đăng ký callback điều hướng cho các controller tương ứng.
   */
  private void initNodes() {
    try {
      registerSubView(LoginViewType.WELCOME, LoginViewType.WELCOME.getFxmlPath());
      registerSubView(LoginViewType.LOGIN, LoginViewType.LOGIN.getFxmlPath());
      registerSubView(LoginViewType.REGISTER, LoginViewType.REGISTER.getFxmlPath());
    } catch (IOException e) {
      LOGGER.error("không thể load Node", e);
    }
  }

  /**
   * Nạp một Node từ file FXML, đưa vào Map và thiết lập callback chuyển đổi view.
   *
   * @param type     Loại view cần đăng ký
   * @param fxmlPath Đường dẫn tới file FXML của view đó
   * @throws IOException Nếu không thể nạp file FXML
   */
  private void registerSubView(LoginViewType type, String fxmlPath) throws IOException {
    NodeLoader loader = new NodeLoader(fxmlPath);
    Node node = loader.getCurrentNode();
    PageController<LoginViewType> controller = loader.getController();


    controller.setSwitchView(this::switchNode);
    nodeMap.put(type, node);
    LOGGER.debug("Registered sub-view: {}", type);
  }

  /**
   * Chuyển đổi hiển thị sang màn hình được chỉ định.
   *
   * @param name Loại màn hình mục tiêu (ví dụ: LOGIN, WELCOME)
   */
  public void switchNode(LoginViewType name) {
    Node node = nodeMap.get(name);
    if (node == null) {
      LOGGER.warn("Node not found: {}", name);
      return;
    }
    getChildren().setAll(nodeMap.get(name));
  }

  public static void switchNextScene() {
    FXThread.dispatch(() -> {
      Scene Home = new Scene(new HomeView());
      SceneManager.getInstance().registerScene(SceneType.HOME ,Home);
      SceneManager.getInstance().switchTo(SceneType.HOME);
    });
  }
}
