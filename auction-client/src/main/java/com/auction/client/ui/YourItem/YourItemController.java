package com.auction.client.ui.YourItem;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;

public class YourItemController {
  @FXML private FlowPane ItemContainer;
  @FXML private javafx.scene.control.Label ActiveItemsValue;
  @FXML private javafx.scene.control.Label InventoryValue;
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
    ItemContainer.getChildren().clear();
    int uid = ClientSession.getCurrentUser() == null ? -1 : ClientSession.getCurrentUser().getid();
    int activeCount = 0;
    double totalValue = 0;
    for (Item item : cachedItems) {
      if (item.getsellerid() != uid) continue;
      if (!match(item)) continue;
      if (item.getstatus() == ItemStatus.OPEN) activeCount++;
      totalValue += item.getcurrentprice();
      try {
        NodeContentLoader<javafx.scene.layout.HBox> loader = new NodeContentLoader<>();
        loader.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController controller = loader.getController();
        if (controller != null) {
          controller.setData(
              safe(item.getname()),
              item.getcurrentprice(),
              safe(item.getdescription()),
              formatStatus(item.getstatus()),
              safe(item.getimageurl()));
        }
        NodeManager.addNodeToPane(loader, ItemContainer);
      } catch (Exception ignored) {
      }
    }
    ActiveItemsValue.setText(String.valueOf(activeCount));
    InventoryValue.setText(String.format("%,.0f$", totalValue));
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

  private String formatStatus(ItemStatus status) {
    if (status == null) return "N/A";
    return status.name();
  }
}
