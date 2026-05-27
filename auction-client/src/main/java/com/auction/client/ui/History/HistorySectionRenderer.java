package com.auction.client.ui.History;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.client.ui.TrangChu.HomeItemCardFactory;
import com.auction.shared.Item;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Incrementally syncs a history section's FlowPane with an item list. */
final class HistorySectionRenderer {

  private final HomeItemCardFactory liveCardFactory;

  HistorySectionRenderer(HomeItemCardFactory liveCardFactory) {
    this.liveCardFactory = liveCardFactory;
  }

  void sync(
      FlowPane pane,
      List<Item> items,
      HistoryPaneCards model,
      Function<Item, String> captionFn,
      boolean liveTrendingCountdown) {
    if (pane == null) {
      return;
    }
    List<Item> ordered = items != null ? items : List.of();
    Map<Integer, ItemCardController> cardMap = model.cards;
    Map<Integer, Node> rootByItemId = model.roots;

    if (ordered.isEmpty()) {
      clearSection(pane, model);
      Label empty = new Label("Empty");
      empty.getStyleClass().add("card-text");
      empty.setUserData(HistoryPaneCards.EMPTY_PLACEHOLDER);
      pane.getChildren().add(empty);
      return;
    }

    pane.getChildren().removeIf(n -> HistoryPaneCards.EMPTY_PLACEHOLDER.equals(n.getUserData()));
    Set<Integer> desiredIds = new HashSet<>(ordered.size() * 2);
    for (Item it : ordered) {
      desiredIds.add(it.getId());
    }
    for (int id : new ArrayList<>(cardMap.keySet())) {
      if (!desiredIds.contains(id)) {
        cardMap.remove(id);
        Node removed = rootByItemId.remove(id);
        if (removed != null) {
          pane.getChildren().remove(removed);
        }
      }
    }

    for (Item item : ordered) {
      ItemCardController card = cardMap.get(item.getId());
      String caption = captionFn.apply(item);
      if (card != null) {
        updateExistingCard(card, item, caption, liveTrendingCountdown);
      } else {
        ItemCardController newCard = createCard(item, caption, liveTrendingCountdown);
        VBox root = newCard != null ? newCard.getRootNode() : null;
        if (newCard != null && root != null) {
          cardMap.put(item.getId(), newCard);
          rootByItemId.put(item.getId(), root);
        }
      }
    }

    applyChildOrder(pane.getChildren(), ordered, rootByItemId);
  }

  private void updateExistingCard(
      ItemCardController card, Item item, String caption, boolean liveTrendingCountdown) {
    if (liveTrendingCountdown) {
      card.syncFromCatalogItem(item);
    } else {
      card.syncFromCatalogItemStaticTime(item, caption);
      card.attachCatalogItem(item);
    }
  }

  private ItemCardController createCard(Item item, String caption, boolean liveTrendingCountdown) {
    if (liveTrendingCountdown) {
      return liveCardFactory.createCard(item, false);
    }
    try {
      NodeContentLoader<VBox> cardLoader = new NodeContentLoader<>();
      cardLoader.load("/fxml/itemcard/ItemCard.fxml");
      ItemCardController newCard = cardLoader.getController();
      VBox root = cardLoader.getCurrentNode();
      if (newCard != null && root != null) {
        newCard.setData(
            item.getId(),
            safe(item.getName()),
            item.getCurrentPrice(),
            safe(item.getDescription()),
            caption,
            safe(item.getImageUrl()),
            safe(item.getSellerUsername()),
            safe(item.getSellerAvatarUrl()));
        newCard.setEndTime(null);
        newCard.attachCatalogItem(item);
        return newCard;
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  private static void clearSection(FlowPane pane, HistoryPaneCards model) {
    for (Node n : new ArrayList<>(model.roots.values())) {
      pane.getChildren().remove(n);
    }
    model.cards.clear();
    model.roots.clear();
    pane.getChildren().clear();
  }

  private static void applyChildOrder(
      ObservableList<Node> children, List<Item> ordered, Map<Integer, Node> rootByItemId) {
    List<Node> orderedNodes = new ArrayList<>(ordered.size());
    for (Item item : ordered) {
      Node n = rootByItemId.get(item.getId());
      if (n != null) {
        orderedNodes.add(n);
      }
    }
    boolean sameOrder = children.size() == orderedNodes.size();
    if (sameOrder) {
      for (int i = 0; i < orderedNodes.size(); i++) {
        if (children.get(i) != orderedNodes.get(i)) {
          sameOrder = false;
          break;
        }
      }
    }
    if (!sameOrder) {
      children.setAll(orderedNodes);
    }
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
