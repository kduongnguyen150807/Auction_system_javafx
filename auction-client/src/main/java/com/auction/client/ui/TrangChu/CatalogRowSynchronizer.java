package com.auction.client.ui.TrangChu;

import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.Item;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Keeps a horizontal or vertical row of item cards in sync with a desired {@link Item} list
 * (diff/add/remove + stable order).
 */
public final class CatalogRowSynchronizer {

  private final HomeItemCardFactory cardFactory;

  public CatalogRowSynchronizer(HomeItemCardFactory cardFactory) {
    this.cardFactory = cardFactory;
  }

  /**
   * @param categoryHBoxSlotSizing when true, treat {@code row} as an {@link HBox} lane and disable
   *     horizontal grow on region children (compact catalog slots).
   */
  public void syncRow(
      Pane row,
      Map<Integer, ItemCardController> cardMap,
      Map<Integer, Node> rootMap,
      List<Item> visible,
      boolean compactCards,
      boolean categoryHBoxSlotSizing) {
    Set<Integer> desiredIds = new HashSet<>();
    for (Item item : visible) {
      desiredIds.add(item.getId());
    }

    for (int id : new ArrayList<>(cardMap.keySet())) {
      if (!desiredIds.contains(id)) {
        cardMap.remove(id);
        Node removed = rootMap.remove(id);
        if (removed != null) {
          row.getChildren().remove(removed);
        }
      }
    }

    for (Item item : visible) {
      ItemCardController card = cardMap.get(item.getId());
      if (card != null) {
        card.syncFromCatalogItem(item);
      } else {
        ItemCardController created = cardFactory.createCard(item, compactCards);
        if (created != null) {
          cardMap.put(item.getId(), created);
          Node root = created.getRootNode();
          rootMap.put(item.getId(), root);
        }
      }
    }

    List<Node> ordered = new ArrayList<>(visible.size());
    for (Item item : visible) {
      Node n = rootMap.get(item.getId());
      if (n != null) {
        ordered.add(n);
      }
    }
    applyChildOrder(row.getChildren(), ordered);

    if (categoryHBoxSlotSizing && row instanceof HBox hBox) {
      for (Node n : hBox.getChildren()) {
        if (n instanceof Region r) {
          HBox.setHgrow(r, Priority.NEVER);
        }
      }
    }
  }

  private void applyChildOrder(ObservableList<Node> children, List<Node> ordered) {
    boolean same = children.size() == ordered.size();
    if (same) {
      for (int i = 0; i < ordered.size(); i++) {
        if (children.get(i) != ordered.get(i)) {
          same = false;
          break;
        }
      }
    }
    if (!same) {
      children.setAll(ordered);
    }
  }
}
