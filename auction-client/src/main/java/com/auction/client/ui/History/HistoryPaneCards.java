package com.auction.client.ui.History;

import com.auction.client.ui.ItemCard.ItemCardController;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Node;

/** Tracks item cards rendered inside a {@link javafx.scene.layout.FlowPane} section. */
final class HistoryPaneCards {
  static final Object EMPTY_PLACEHOLDER = new Object();

  final Map<Integer, ItemCardController> cards = new HashMap<>();
  final Map<Integer, Node> roots = new HashMap<>();
}
