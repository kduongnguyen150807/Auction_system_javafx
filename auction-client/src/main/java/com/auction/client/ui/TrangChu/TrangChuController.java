package com.auction.client.ui.TrangChu;

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

public class TrangChuController {
  private static TrangChuController instance;
  @FXML private HBox TrendingBind;
  private final List<Item> cacheditems = new ArrayList<>();
  private final Map<Integer, ItemCardController> cardmap = new HashMap<>();
  private String kw = "";
  private String cat = "All";

  public static TrangChuController getInstance() {
    TrangChuController ans = instance;
    return ans;
  }

  @FXML
  void initialize() {
    instance = this;
    setFilters(KhungController.getSearchKeyword(), KhungController.getCategoryFilter());
    refreshItems();
  }

  public void refreshItems() {
    Thread ans =
        new Thread(
            () -> {
              try {
                Request res = new Request(Request.GET_ONGOING_LOTS, null);
                Response ans1 = NetworkClient.getInstance().sendRequestAndWait(res);
                if (ans1 == null || !Response.OK.equals(ans1.getStatus())) return;
                Object res1 = ans1.getPayload();
                if (!(res1 instanceof List<?> list)) return;
                Platform.runLater(() -> cacheAndRender(list));
              } catch (Exception e) {
              }
            });
    ans.setDaemon(true);
    ans.start();
  }

  public void setFilters(String k, String c) {
    this.kw = (k == null) ? "" : k.trim().toLowerCase();
    this.cat = (c == null || c.isBlank()) ? "All" : c;
    renderFilteredItems();
  }

  private void cacheAndRender(List<?> list) {
    cacheditems.clear();
    for (Object ans : list) {
      if (ans instanceof Item i) cacheditems.add(i);
    }
    renderFilteredItems();
  }

  private void renderFilteredItems() {
    if (TrendingBind == null) return;
    TrendingBind.getChildren().clear();
    cardmap.clear();
    for (Item ans : cacheditems) {
      if (!match(ans)) continue;
      try {
        NodeContentLoader<HBox> res = new NodeContentLoader<>();
        res.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController ans1 = res.getController();
        if (ans1 != null) {
          ans1.setData(
              ans.getId(),
              safe(ans.getName()),
              ans.getCurrentPrice(),
              safe(ans.getDescription()),
              formatTime(ans.getEndTime()),
              safe(ans.getImageUrl()),
              safe(ans.getSellerUsername()),
              safe(ans.getSellerAvatarUrl()));
          cardmap.put(ans.getId(), ans1);
        }
        NodeManager.addNodeToPane(res, TrendingBind);
      } catch (Exception e) {
      }
    }
  }

  public void updatePriceUi(Item ans) {
    if (ans == null) return;
    updateItemPrice(ans);
  }

  public void updateItemPrice(Item ans) {
    for (int res = 0; res < cacheditems.size(); res++) {
      if (cacheditems.get(res).getId() == ans.getId()) {
        cacheditems.set(res, ans);
        break;
      }
    }
    ItemCardController res1 = cardmap.get(ans.getId());
    if (res1 != null) {
      res1.setData(
          ans.getId(),
          safe(ans.getName()),
          ans.getCurrentPrice(),
          safe(ans.getDescription()),
          formatTime(ans.getEndTime()),
          safe(ans.getImageUrl()),
          safe(ans.getSellerUsername()),
          safe(ans.getSellerAvatarUrl()));
    }
  }

  private boolean match(Item ans) {
    String res = safe(ans.getName()).toLowerCase();
    if (!kw.isBlank() && !res.contains(kw)) return false;
    double ans1 = KhungController.getMinPrice();
    double res1 = KhungController.getMaxPrice();
    if (ans.getCurrentPrice() < ans1 || ans.getCurrentPrice() > res1) return false;
    if ("All".equalsIgnoreCase(cat)) return true;
    return safe(ans.getCategory()).equalsIgnoreCase(cat);
  }

  private String safe(String ans) {
    String res = (ans == null) ? "" : ans;
    return res;
  }

  private String formatTime(LocalDateTime ans) {
    if (ans == null) return "N/A";
    Duration res = Duration.between(LocalDateTime.now(), ans);
    if (res.isNegative() || res.isZero()) return "closed";
    long ans1 = res.toHours();
    if (ans1 / 24 > 0) return (ans1 / 24) + "d " + (ans1 % 24) + "h";
    return (ans1 % 24) + "h " + (res.toMinutes() % 60) + "m";
  }
}
