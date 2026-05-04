package com.auction.client.ui.TrangChu;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrangChuController {
  private static final Logger LOGGER = LoggerFactory.getLogger(TrangChuController.class);
  private static final int AUTO_REFRESH_SECONDS = 30;
  private static TrangChuController instance;

  @FXML private HBox TrendingBind;
  private final List<Item> cachedItems = new ArrayList<>();
  private final Map<Integer, ItemCardController> cardMap = new HashMap<>();
  /** Card root nodes in the home strip (VBox from ItemCard.fxml), keyed by item id. */
  private final Map<Integer, Node> cardRootByItemId = new HashMap<>();
  private String keyword = "";
  private String category = "All";
  private Timeline countdownTimeline;
  private Timeline autoRefreshTimeline;

  public static TrangChuController getInstance() {
    return instance;
  }

  @FXML
  void initialize() {
    if (instance != null) instance.stopTimelines();
    instance = this;
    setFilters(KhungController.getSearchKeyword(), KhungController.getCategoryFilter());
    refreshItems();
    startTimelines();
  }

  private void startTimelines() {
    countdownTimeline = new Timeline(
        new KeyFrame(javafx.util.Duration.seconds(1), e -> cardMap.values().forEach(ItemCardController::updateTimeLabel)));
    countdownTimeline.setCycleCount(Timeline.INDEFINITE);
    countdownTimeline.play();

    autoRefreshTimeline = new Timeline(
        new KeyFrame(javafx.util.Duration.seconds(AUTO_REFRESH_SECONDS), e -> refreshItems()));
    autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
    autoRefreshTimeline.play();
  }

  public void stopTimelines() {
    if (countdownTimeline != null) { countdownTimeline.stop(); countdownTimeline = null; }
    if (autoRefreshTimeline != null) { autoRefreshTimeline.stop(); autoRefreshTimeline = null; }
  }

  public void refreshItems() {
    Thread fetchThread = new Thread(() -> {
      try {
        int userId =
            ClientSession.getCurrentUser() != null
                ? ClientSession.getCurrentUser().getId()
                : 0;
        Request request = new Request(Request.GET_ONGOING_BIDS, userId);
        Response response = NetworkClient.getInstance().sendRequestAndWait(request);
        if (response == null || !Response.OK.equals(response.getStatus())) return;
        Object payload = response.getPayload();
        if (!(payload instanceof List<?> list)) return;
        Platform.runLater(() -> cacheAndRender(list));
      } catch (Exception e) {
        LOGGER.warn("Failed to refresh auction items", e);
      }
    });
    fetchThread.setDaemon(true);
    fetchThread.start();
  }

  public void setFilters(String keyword, String category) {
    this.keyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
    this.category = (category == null || category.isBlank()) ? "All" : category;
    renderFilteredItems();
  }

  private void cacheAndRender(List<?> rawList) {
    cachedItems.clear();
    for (Object entry : rawList) {
      if (entry instanceof Item item) {
        cachedItems.add(item);
      }
    }
    renderFilteredItems();
  }

  private void renderFilteredItems() {
    if (TrendingBind == null) return;

    List<Item> visible = new ArrayList<>();
    for (Item item : cachedItems) {
      if (matchesFilter(item)) visible.add(item);
    }
    Set<Integer> desiredIds = new HashSet<>(visible.size() * 2);
    for (Item item : visible) desiredIds.add(item.getId());

    for (int id : new ArrayList<>(cardMap.keySet())) {
      if (!desiredIds.contains(id)) {
        cardMap.remove(id);
        Node removed = cardRootByItemId.remove(id);
        if (removed != null) TrendingBind.getChildren().remove(removed);
      }
    }

    for (Item item : visible) {
      ItemCardController card = cardMap.get(item.getId());
      if (card != null) {
        card.syncFromCatalogItem(item);
      } else {
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
                formatTime(item.getEndTime()),
                safe(item.getImageUrl()),
                safe(item.getSellerUsername()),
                safe(item.getSellerAvatarUrl()));
            newCard.setEndTime(item.getEndTime());
            cardMap.put(item.getId(), newCard);
            cardRootByItemId.put(item.getId(), root);
          }
        } catch (Exception e) {
          LOGGER.warn("Failed to render item card for item id={}", item.getId(), e);
        }
      }
    }

    List<Node> ordered = new ArrayList<>(visible.size());
    for (Item item : visible) {
      Node n = cardRootByItemId.get(item.getId());
      if (n != null) ordered.add(n);
    }
    ObservableList<Node> children = TrendingBind.getChildren();
    boolean same = children.size() == ordered.size();
    if (same) {
      for (int i = 0; i < ordered.size(); i++) {
        if (children.get(i) != ordered.get(i)) {
          same = false;
          break;
        }
      }
    }
    if (!same) children.setAll(ordered);
  }

  public void removeClosedItem(Item item) {
    if (item == null) return;
    cachedItems.removeIf(cached -> cached.getId() == item.getId());
    renderFilteredItems();
  }

  public void updatePriceUi(Item updated) {
    if (updated == null) return;
    Item cachedRef = null;
    for (Item cached : cachedItems) {
      if (cached.getId() == updated.getId()) {
        cached.setCurrentPrice(updated.getCurrentPrice());
        cached.setEndTime(updated.getEndTime());
        cachedRef = cached;
        break;
      }
    }
    ItemCardController card = cardMap.get(updated.getId());
    if (card != null && cachedRef != null) card.syncFromCatalogItem(cachedRef);
  }

  private boolean matchesFilter(Item item) {
    String name = safe(item.getName()).toLowerCase();
    if (!keyword.isBlank() && !name.contains(keyword)) return false;
    double minPrice = KhungController.getMinPrice();
    double maxPrice = KhungController.getMaxPrice();
    if (maxPrice <= 0) maxPrice = Double.MAX_VALUE;
    if (item.getCurrentPrice() < minPrice || item.getCurrentPrice() > maxPrice) return false;
    return true;
  }

  private String safe(String value) {
    return (value == null) ? "" : value;
  }

  private String formatTime(LocalDateTime endTime) {
    if (endTime == null) return "N/A";
    Duration remaining = Duration.between(LocalDateTime.now(), endTime);
    if (remaining.isNegative() || remaining.isZero()) return "closed";
    long hours = remaining.toHours();
    if (hours / 24 > 0) return (hours / 24) + "d " + (hours % 24) + "h";
    return (hours % 24) + "h " + (remaining.toMinutes() % 60) + "m";
  }
}
