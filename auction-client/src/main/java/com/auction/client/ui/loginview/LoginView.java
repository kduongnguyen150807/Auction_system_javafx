package com.auction.client.ui.loginview;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.ui.base.CanSwitchNode;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class LoginView extends StackPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoginView.class);

  private final String BASE_FXML_PATH = "/fxml/loginview/LoginView.fxml";

  private final Map<LoginViewType, Node> nodeMap = new EnumMap<>(LoginViewType.class);

  public LoginView() {
    initBaseLayout();
    initNodes();
    switchNode(LoginViewType.WELCOME);
  }

  private void initBaseLayout() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    loader.setController(this);
    loader.setRoot(this);

    try {
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void initNodes() {
    try {
      registerSubView(LoginViewType.WELCOME, LoginViewType.WELCOME.getFxmlPath());
    } catch (IOException e) {
      LOGGER.error("Failed to load FXML loader.", e);
    }
  }

  private void registerSubView(LoginViewType viewType, String fxmlPath) throws IOException {
    if (nodeMap.containsKey(viewType)) {
      throw new IllegalStateException("View already registered: " + viewType);
    }

    NodeContentLoader loader = new NodeContentLoader();
    loader.load(fxmlPath);

    Node node = loader.getCurrentNode();

    Object controller = loader.getController();
    if (controller instanceof CanSwitchNode<?> csn) {
      @SuppressWarnings("unchecked")
      CanSwitchNode<LoginViewType> typed = (CanSwitchNode<LoginViewType>) csn;
      typed.setSwitchNode(this::switchNode);
    }

    nodeMap.put(viewType, node);
  }

  public void switchNode(LoginViewType viewType) {
    Node node = nodeMap.get(viewType);
    if (node != null) {
      getChildren().setAll(node);
    } else {
      try {
        registerSubView(viewType, viewType.getFxmlPath());
        switchNode(viewType);
      } catch (IOException e) {
        LOGGER.error("Failed to load FXML loader.", e);
      }
    }
  }
}
