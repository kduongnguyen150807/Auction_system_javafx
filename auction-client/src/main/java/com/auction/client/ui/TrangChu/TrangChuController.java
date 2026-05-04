package com.auction.client.ui.TrangChu;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
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
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrangChuController {
  private static final Logger LOGGER = LoggerFactory.getLogger(TrangChuController.class);
  private static TrangChuController instance;

  @FXML private HBox TrendingBind;
  private final List<Item> cachedItems = new ArrayList<>();
  private final Map<Integer, ItemCardController> cardMap = new HashMap<>();
  private String keyword = "";
  private String category = "All";

  public static TrangChuController getInstance() {
    return instance;
  }

  @FXML
  void initialize() {
    instance = this;
    setFilters(KhungController.getSearchKeyword(), KhungController.getCategoryFilter());
    refreshItems();
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

  public void setFilters(String kw, String cat) {
    this.keyword = (kw == null) ? "" : kw.trim().toLowerCase();
    this.category = (cat == null || cat.isBlank()) ? "All" : cat;
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
    TrendingBind.getChildren().clear();
    cardMap.clear();
    for (Item item : cachedItems) {
      if (!matchesFilter(item)) continue;
      try {
        NodeContentLoader<HBox> cardLoader = new NodeContentLoader<>();
        cardLoader.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController card = cardLoader.getController();
        if (card != null) {
          card.setData(
              item.getId(),
              safe(item.getName()),
              item.getCurrentPrice(),
              safe(item.getDescription()),
              formatTime(item.getEndTime()),
              safe(item.getImageUrl()),
              safe(item.getSellerUsername()),
              safe(item.getSellerAvatarUrl()));
          cardMap.put(item.getId(), card);
        }
        NodeManager.addNodeToPane(cardLoader, TrendingBind);
      } catch (Exception e) {
        LOGGER.warn("Failed to render item card for item id={}", item.getId(), e);
      }
    }
  }

  public void removeClosedItem(Item item) {
    if (item == null) return;
    cachedItems.removeIf(cached -> cached.getId() == item.getId());
    cardMap.remove(item.getId());
    renderFilteredItems();
  }

  public void updatePriceUi(Item updated) {
    if (updated == null) return;
    for (Item cached : cachedItems) {
      if (cached.getId() == updated.getId()) {
        cached.setCurrentPrice(updated.getCurrentPrice());
        cached.setEndTime(updated.getEndTime());
        break;
      }
    }
    ItemCardController card = cardMap.get(updated.getId());
    if (card != null) {
      card.setData(
          updated.getId(),
          safe(updated.getName()),
          updated.getCurrentPrice(),
          safe(updated.getDescription()),
          formatTime(updated.getEndTime()),
          safe(updated.getImageUrl()),
          safe(updated.getSellerUsername()),
          safe(updated.getSellerAvatarUrl()));
    }
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
