package com.auction.client.ui.TrangChu;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.AuctionType;
import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Item;
import java.time.Duration;
import java.time.LocalDateTime;
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
      newCard.attachCatalogItem(item);
      newCard.setData(
          item.getId(),
          safe(item.getName()),
          item.getCurrentPrice(),
          safe(item.getDescription()),
          formatRemainingForItem(item),
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

  /** Countdown toward auction end (English) or next drop / end (Dutch). */
  private static String formatRemainingForItem(Item item) {
    if (item == null) {
      return "N/A";
    }
    LocalDateTime now = LocalDateTime.now();
    if (item.getAuctionType() == AuctionType.DUTCH) {
      LocalDateTime target = DutchAuctionPricing.countdownTarget(item, now);
      return DutchAuctionPricing.formatShortCountdownToward(target, now);
    }
    return formatRemaining(item.getEndTime());
  }

  private static String formatRemaining(LocalDateTime endTime) {
    if (endTime == null) {
      return "N/A";
    }
    Duration remaining = Duration.between(LocalDateTime.now(), endTime);
    if (remaining.isNegative() || remaining.isZero()) {
      return "closed";
    }
    long hours = remaining.toHours();
    if (hours / 24 > 0) {
      return (hours / 24) + "d " + (hours % 24) + "h";
    }
    return (hours % 24) + "h " + (remaining.toMinutes() % 60) + "m";
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
