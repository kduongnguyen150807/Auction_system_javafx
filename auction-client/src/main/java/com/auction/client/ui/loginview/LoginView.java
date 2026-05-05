package com.auction.client.ui.loginview;

import com.auction.client.app.NodeLoader;
import com.auction.client.ui.PageController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

  /** Lưu trữ các instance của Node tương ứng với từng loại màn hình */
  private final Map<LoginViewType, Node> nodeMap = new HashMap<>();

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
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView/LoginView.fxml"));
    loader.setRoot(this);
    loader.setController(this);

    String cssPath = getClass().getResource("/css/LoginView/LoginView.css").toExternalForm();
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
      registerSubView(LoginViewType.WELCOME, "/fxml/LoginView/Welcome.fxml");

      registerSubView(LoginViewType.LOGIN, "/fxml/LoginView/Login.fxml");
    } catch (IOException e) {
      LOGGER.error("không thể load Node", e);
    }
  }

  /**
   * Nạp một Node từ file FXML, đưa vào Map và thiết lập callback chuyển đổi view.
   *
   * @param type Loại view cần đăng ký
   * @param fxmlPath Đường dẫn tới file FXML của view đó
   * @throws IOException Nếu không thể nạp file FXML
   */
  private void registerSubView(LoginViewType type, String fxmlPath) throws IOException {
    NodeLoader loader = new NodeLoader(fxmlPath);
    Node node = loader.getCurrentNode();
    Object controller = loader.getController();

    if (controller instanceof PageController subController) {
      subController.setSwitchView(this::switchNode);
    }

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
}
