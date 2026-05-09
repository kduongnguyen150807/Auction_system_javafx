package com.auction.client.ui.YourItem;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class YourItemController {
  @FXML private FlowPane ItemContainer;
  @FXML private Label ActiveItemsValue, InventoryValue;

  private final Map<Integer, ItemCardController> cardMap = new HashMap<>();
  private final Map<Integer, Node> cardRootByItemId = new HashMap<>();

  @FXML
  void initialize() {
    refreshItems();
  }

  @FXML
  public void refreshItems() {
    if (ClientSession.getCurrentUser() == null) return;
    int userId = ClientSession.getCurrentUser().getId();

    new Thread(
            () -> {
              try {
                Request request = new Request("get_my_items", userId);
                Response response = NetworkClient.getInstance().sendRequestAndWait(request);

                if (response != null && Response.OK.equals(response.getStatus())) {
                  List<Item> items = (List<Item>) response.getPayload();
                  Platform.runLater(() -> render(items));
                }
              } catch (Exception ignored) {
              }
            })
        .start();
  }

  private void render(List<Item> items) {
    if (ItemContainer == null) return;
    if (items == null) items = List.of();

    List<Item> visible = new ArrayList<>();
    int openCount = 0;
    double inventoryTotal = 0;

    for (Item item : items) {
      if (!match(item)) continue;
      visible.add(item);
      if (item.getStatus() == ItemStatus.OPEN) openCount++;
      inventoryTotal += item.getCurrentPrice();
    }

    incrementalRender(visible);
    if (ActiveItemsValue != null) ActiveItemsValue.setText(String.valueOf(openCount));
    if (InventoryValue != null) InventoryValue.setText(String.format("%,.0f$", inventoryTotal));
  }

  private String statusCaption(Item item) {
    String caption = item.getStatus() == null ? "N/A" : item.getStatus().name();
    if (item.getStatus() == ItemStatus.PENDING) caption = "\u23F3 Pending Approval";
    return caption;
  }

  private void incrementalRender(List<Item> visible) {
    FlowPane pane = ItemContainer;
    Map<Integer, ItemCardController> cards = cardMap;
    Map<Integer, Node> rootByItemId = cardRootByItemId;

    if (visible.isEmpty()) {
      for (Node n : new ArrayList<>(rootByItemId.values())) {
        pane.getChildren().remove(n);
      }
      cards.clear();
      rootByItemId.clear();
      pane.getChildren().clear();
      return;
    }

    Set<Integer> desiredIds = new HashSet<>(visible.size() * 2);
    for (Item it : visible) desiredIds.add(it.getId());

    for (int id : new ArrayList<>(cards.keySet())) {
      if (!desiredIds.contains(id)) {
        cards.remove(id);
        Node removed = rootByItemId.remove(id);
        if (removed != null) pane.getChildren().remove(removed);
      }
    }

    for (Item item : visible) {
      ItemCardController card = cards.get(item.getId());
      String caption = statusCaption(item);
      if (card != null) {
        card.syncFromCatalogItemStaticTime(item, caption);
      } else {
        try {
          NodeContentLoader<VBox> cardLoader = new NodeContentLoader<>();
          cardLoader.load("/fxml/itemcard/ItemCard.fxml");
          ItemCardController newCard = cardLoader.getController();
          VBox root = cardLoader.getCurrentNode();
          if (newCard != null && root != null) {
            newCard.setData(
                item.getId(),
                item.getName() != null ? item.getName() : "",
                item.getCurrentPrice(),
                item.getDescription() != null ? item.getDescription() : "",
                caption,
                item.getImageUrl() != null ? item.getImageUrl() : "",
                item.getSellerUsername() != null ? item.getSellerUsername() : "",
                item.getSellerAvatarUrl() != null ? item.getSellerAvatarUrl() : "");
            newCard.setEndTime(null);
            cards.put(item.getId(), newCard);
            rootByItemId.put(item.getId(), root);
          }
        } catch (Exception ignored) {
        }
      }
    }

    List<Node> orderedNodes = new ArrayList<>(visible.size());
    for (Item item : visible) {
      Node n = rootByItemId.get(item.getId());
      if (n != null) orderedNodes.add(n);
    }
    ObservableList<Node> children = pane.getChildren();
    boolean sameOrder = children.size() == orderedNodes.size();
    if (sameOrder) {
      for (int i = 0; i < orderedNodes.size(); i++) {
        if (children.get(i) != orderedNodes.get(i)) {
          sameOrder = false;
          break;
        }
      }
    }
    if (!sameOrder) children.setAll(orderedNodes);
  }

  private boolean match(Item item) {
    String keyword = com.auction.client.ui.Main.KhungController.getSearchKeyword();
    String category = com.auction.client.ui.Main.KhungController.getCategoryFilter();
    double minPrice = com.auction.client.ui.Main.KhungController.getMinPrice();
    double maxPrice = com.auction.client.ui.Main.KhungController.getMaxPrice();

    if (keyword != null
        && !keyword.isBlank()
        && item.getName() != null
        && !item.getName().toLowerCase().contains(keyword.toLowerCase())) return false;
    if (category != null
        && !category.equalsIgnoreCase("All")
        && item.getCategory() != null
        && !item.getCategory().equalsIgnoreCase(category)) return false;
    if (item.getCurrentPrice() < minPrice || item.getCurrentPrice() > maxPrice) return false;
    return true;
  }
}
