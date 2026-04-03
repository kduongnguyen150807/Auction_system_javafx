package com.auction.client.ui.TrangChu;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class TrangChuController {
  @FXML private HBox TrendingBind;
  private final List<Item> cachedItems = new ArrayList<>();
  private String keyword = "";
  private String category = "All";

  @FXML
  void initialize() {
    setFilters(KhungController.getSearchKeyword(), KhungController.getCategoryFilter());
    refreshItems();
  }

  public void refreshItems() {
    Thread worker =
        new Thread(
            () -> {
              try {
                Response res =
                    NetworkClient.getinstance()
                        .sendrequestandwait(new Request(Request.list, null));
                if (res == null || !Response.ok.equals(res.getstatus())) return;
                Object payload = res.getpayload();
                if (!(payload instanceof List<?> rawItems)) return;
                Platform.runLater(() -> cacheAndRender(rawItems));
              } catch (Exception ignored) {
              }
            });
    worker.setDaemon(true);
    worker.start();
  }

  public void setFilters(String keyword, String category) {
    this.keyword = keyword == null ? "" : keyword.trim().toLowerCase();
    this.category = (category == null || category.isBlank()) ? "All" : category;
    renderFilteredItems();
  }

  private void cacheAndRender(List<?> rawItems) {
    cachedItems.clear();
    for (Object obj : rawItems) {
      if (obj instanceof Item item) cachedItems.add(item);
    }
    renderFilteredItems();
  }

  private void renderFilteredItems() {
    TrendingBind.getChildren().clear();
    for (Item item : cachedItems) {
      if (!match(item)) continue;
      try {
        NodeContentLoader<HBox> loader = new NodeContentLoader<>();
        loader.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController controller = loader.getController();
        if (controller != null) {
          controller.setData(
              item.getid(),
              safe(item.getname()),
              item.getcurrentprice(),
              safe(item.getdescription()),
              formatTimeRemaining(item.getendtime()),
              safe(item.getimageurl()));
        }
        NodeManager.addNodeToPane(loader, TrendingBind);
      } catch (Exception ignored) {
      }
    }
  }

  private boolean match(Item item) {
    String name = safe(item.getname()).toLowerCase();
    if (!keyword.isBlank() && !name.contains(keyword)) return false;
    if ("All".equalsIgnoreCase(category)) return true;
    return safe(item.getcategory()).equalsIgnoreCase(category);
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }

  private String formatTimeRemaining(LocalDateTime endTime) {
    if (endTime == null) return "N/A";
    Duration d = Duration.between(LocalDateTime.now(), endTime);
    if (d.isNegative() || d.isZero()) return "Da ket thuc";
    long totalHours = d.toHours();
    long days = totalHours / 24;
    long hours = totalHours % 24;
    if (days > 0) return days + "d " + hours + "h";
    long minutes = d.toMinutes() % 60;
    return hours + "h " + minutes + "m";
  }
}
