package com.auction.client.ui.homeview;

import com.auction.client.AppContainer;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.network.NetworkClient;
import com.auction.client.service.user.AuthService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.ResultStore;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.homeview.controller.ContentTable;
import com.auction.client.ui.homeview.homeviewcomponent.NotificationBell;
import com.auction.client.ui.homeview.homeviewcomponent.SearchBar;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HomeView extends HBox {
  private static final Logger LOGGER = LoggerFactory.getLogger(HomeView.class);

  private static final String BASE_FXML_PATH = "/fxml/homeview/HomeView.fxml";
  private static final String BASE_STYLESHEET_PATH = "/css/homeview/HomeView.css";
  private static final String GLOBAL_COLOR_STYLESHEET_PATH = "/css/style.css";

  private final Map<HomeViewType, Node> nodeMap = new EnumMap<>(HomeViewType.class);
  private final Map<HomeViewType, Object> controllerMap = new EnumMap<>(HomeViewType.class);

  @FXML
  private StackPane contentPanel;

  @FXML
  private HBox searchContainer;

  @FXML
  private VBox sideBar;

  public HomeView() {
    initBaseLayout();
    initNodes();
    switchNode(HomeViewType.TRANG_CHU);
  }

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

    } catch (IOException e) {
      LOGGER.error("Failed to load LoginView.fxml", e);
      throw new RuntimeException("Critical UI load failure", e);
    }
  }

  /**
   * Khởi tạo các sub-view tương ứng với từng enum.
   */
  private void initNodes() {
    ContentTable contentTable = new ContentTable(AppContainer.getService(AuthService.class));
    contentTable.setSwitchNode(this::switchNode);
    sideBar.getChildren().add(contentTable);

    NotificationBell notificationBell = new NotificationBell();
    searchContainer.getChildren().add(notificationBell);
    NetworkClient.getInstance().registerNotifcationBell(notificationBell);

    SearchBar<ItemModel> searchBar = new SearchBar<>();
    searchContainer.getChildren().add(searchBar);
    searchBar.setOnSearch(query -> {
        ResultStore.RESULT_STORE.filterWords(Arrays.asList(query));
        switchNode(HomeViewType.RESULT_PAGE);
      }
    );
  }

  /**
   * Nạp một Node từ file FXML, đưa vào Map và thiết lập callback chuyển đổi view.
   *
   * @param type     Loại view cần đăng ký
   * @param fxmlPath Đường dẫn tới file FXML của view đó
   * @throws IOException Nếu không thể nạp file FXML
   */
  private void registerSubView(HomeViewType type, String fxmlPath) throws IOException {
    NodeContentLoader loader = new NodeContentLoader();
    loader.load(fxmlPath);

    Node node = loader.getCurrentNode();

    Object controller = loader.getController();
    if (controller instanceof CanSwitchNode<?> hvt) {
      @SuppressWarnings("unchecked")
      CanSwitchNode<HomeViewType> typed = (CanSwitchNode<HomeViewType>) hvt;
      typed.setSwitchNode(this::switchNode);
    }

    nodeMap.put(type, node);
    controllerMap.put(type, controller);
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
        throw new RuntimeException(e);
      }
    }

    /* refresh data for {@code CanRefresh} controller type */
    Object controller = controllerMap.get(type);
    if (controller instanceof CanRefresh cr) {
      CompletableFuture.runAsync(cr::refreshData);
      LOGGER.info("Refresh complete for type {}", type);
    }
    contentPanel.getChildren().setAll(node);
  }

  @FXML
  private void handleRefresh() {
    for (Map.Entry<HomeViewType, Object> entry : controllerMap.entrySet()) {
      Object controller = controllerMap.get(entry.getKey());
      if (controller instanceof CanRefresh cr) {
        CompletableFuture.runAsync(cr::refreshData);
        LOGGER.info("Refresh complete for type {}", entry.getKey());
      }
    }
  }
}
