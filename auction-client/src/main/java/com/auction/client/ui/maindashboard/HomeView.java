package com.auction.client.ui.maindashboard;

import com.auction.client.app.NodeLoader;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.PageController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * View chính của màn hình Home (Dashboard).
 *
 * <p>Chức năng:
 * <ul>
 *   <li>Load layout chính từ FXML</li>
 *   <li>Quản lý sidebar (ContentTable)</li>
 *   <li>Quản lý các sub-view tương ứng với từng {@link HomeViewType}</li>
 *   <li>Điều hướng giữa các sub-view thông qua {@code switchNode}</li>
 * </ul>
 *
 * <p>Thiết kế:
 * <ul>
 *   <li>Sử dụng {@link EnumMap} để lưu trữ các view</li>
 *   <li>Inject callback navigation xuống các controller con</li>
 *   <li>Container {@link StackPane} dùng để thay đổi nội dung hiển thị</li>
 * </ul>
 */
public class HomeView extends HBox {
  private static final Logger LOGGER = LoggerFactory.getLogger(HomeView.class);

  private static final String BASE_FXML_PATH = "/fxml/MainDashBoard/HomeView.fxml";
  private static final String BASE_STYLESHEET_PATH = "/css/MainDashBoard/HomeView.css";
  private static final String GLOBAL_COLOR_STYLESHEET_PATH = "/css/GlobalColor.css";
  private static final String CONTENT_TABLE_FXML_PATH = "/fxml/MainDashBoard/ContentTable.fxml";

  /**
   * Lưu trữ các node tương ứng với từng type
   */
  private final Map<HomeViewType, Node> nodeMap = new EnumMap<>(HomeViewType.class);

  /**
   * Lưu trữ controller tương ứng với từng type
   */
  private final Map<HomeViewType, Object> controllerMap = new EnumMap<>(HomeViewType.class);

  /**
   * Panel hiển thị nội dung chính
   */
  @FXML
  private StackPane contentPanel;

  @FXML
  private StackPane contentButtonTable;

  /**
   * Constructor khởi tạo HomeView.
   */
  public HomeView() {
    initBaseLayout();
    initNodes();
    switchNode(HomeViewType.AUCTION);
  }

  /**
   * Khởi tạo layout chính:
   * - Load FXML
   * - Load sidebar
   * - Gắn stylesheet
   */
  private void initBaseLayout() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    loader.setRoot(this);
    loader.setController(this);

    this.getStylesheets().addAll(
      getClass().getResource(BASE_STYLESHEET_PATH).toExternalForm(),
      getClass().getResource(GLOBAL_COLOR_STYLESHEET_PATH).toExternalForm()
    );
    try {
      loader.load();
      LOGGER.info("HomeView base layout loaded successfully.");

      NodeLoader contentTableLoader = new NodeLoader(CONTENT_TABLE_FXML_PATH);
      contentButtonTable.getChildren().add(contentTableLoader.getCurrentNode());
      if (contentTableLoader.getController() instanceof PageController<?> pc) {
        @SuppressWarnings("unchecked")
        PageController<HomeViewType> subController = (PageController<HomeViewType>) pc;
        subController.setSwitchView(this::switchNode);
      }
    } catch (IOException e) {
      LOGGER.error("Failed to load LoginView.fxml", e);
      throw new RuntimeException("Critical UI load failure", e);
    }
  }

  /**
   * Khởi tạo các sub-view tương ứng với từng enum.
   */
  private void initNodes() {
    LOGGER.info("this method doesnt actually does anything right now");
  }

  /**
   * Nạp một Node từ file FXML, đưa vào Map và thiết lập callback chuyển đổi view.
   *
   * @param type     Loại view cần đăng ký
   * @param fxmlPath Đường dẫn tới file FXML của view đó
   * @throws IOException Nếu không thể nạp file FXML
   */
  private void registerSubView(HomeViewType type, String fxmlPath) throws IOException {
    NodeLoader loader = new NodeLoader(fxmlPath);
    Node node = loader.getCurrentNode();
    Object controller = loader.getController();

    if (loader.getController() instanceof PageController<?> pc) {
      PageController<HomeViewType> subController = (PageController<HomeViewType>) pc;
      subController.setSwitchView(this::switchNode);
    }

    controllerMap.put(type, controller);
    nodeMap.put(type, node);
    LOGGER.debug("Registered sub-view: {}", type);
  }

  public void switchNode(HomeViewType type) {
    Node node = nodeMap.get(type);

    /* Lazy loading */
    if (node == null) {
      try {
        registerSubView(type, type.getFxmlPath());
        node = nodeMap.get(type);
      } catch (Exception e) {
        LOGGER.warn("Node not found: {}", type);
        return;
      }
    }

    /* refresh data for {@code CanRefresh} controller type */
    Object controller = controllerMap.get(type);
    if (controller instanceof CanRefresh cr) {
      CompletableFuture.runAsync(cr::refreshData);
    }
    contentPanel.getChildren().setAll(node);
  }
}
