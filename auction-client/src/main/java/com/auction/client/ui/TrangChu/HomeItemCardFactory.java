package com.auction.client.ui.TrangChu;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.Item;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads {@link ItemCardController} nodes from FXML for catalog rows. */
public final class HomeItemCardFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(HomeItemCardFactory.class);

  public ItemCardController createCard(Item item, boolean compact) {
    try {
      NodeContentLoader<VBox> cardLoader = new NodeContentLoader<>();
      cardLoader.load("/fxml/itemcard/ItemCard.fxml");
      ItemCardController newCard = cardLoader.getController();
      VBox root = cardLoader.getCurrentNode();
      if (newCard == null || root == null) {
        return null;
      }
      newCard.setData(
          item.getId(),
          safe(item.getName()),
          item.getCurrentPrice(),
          safe(item.getDescription()),
          CatalogTimeFormatter.formatRemaining(item.getEndTime()),
          safe(item.getImageUrl()),
          safe(item.getSellerUsername()),
          safe(item.getSellerAvatarUrl()));
      newCard.setEndTime(item.getEndTime());
      newCard.setCompactRowLayout(compact);
      return newCard;
    } catch (Exception e) {
      LOGGER.warn("Failed to render item card for item id={}", item.getId(), e);
      return null;
    }
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
