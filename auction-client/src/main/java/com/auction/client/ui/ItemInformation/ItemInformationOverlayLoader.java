package com.auction.client.ui.ItemInformation;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
import java.util.function.Consumer;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ItemInformationOverlayLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemInformationOverlayLoader.class);

  private ItemInformationOverlayLoader() {}

  static void addToMainContent(String fxmlPath, Consumer<Object> setupController) {
    try {
      NodeContentLoader<VBox> l = new NodeContentLoader<>();
      l.load(fxmlPath);
      setupController.accept(l.getController());
      NodeManager.addNodeToPane(l, KhungController.getMainContentPane());
    } catch (Exception e) {
      LOGGER.warn("Failed to load overlay: {}", fxmlPath, e);
    }
  }
}
