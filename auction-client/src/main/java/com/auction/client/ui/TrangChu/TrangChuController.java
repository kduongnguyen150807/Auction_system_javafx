package com.auction.client.ui.TrangChu;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Item;
import com.auction.shared.Lot;
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
  private final List<Lot> cacheditems = new ArrayList<>();
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
                        int res1 =
                                ClientSession.getCurrentUser() != null
                                        ? ClientSession.getCurrentUser().getId()
                                        : 0;
                        Request res = new Request(Request.GET_ONGOING_BIDS, res1);
                        Response ans1 = NetworkClient.getInstance().sendRequestAndWait(res);
                        if (ans1 == null || !Response.OK.equals(ans1.getStatus())) return;
                        Object ans2 = ans1.getPayload();
                        if (!(ans2 instanceof List<?> list)) return;
                        Platform.runLater(() -> cacheAndRender(list));
                      } catch (Exception e) {
                      }
                    });
    ans.setDaemon(true);
    ans.start();
  }

  public void setFilters(String res, String ans) {
    this.kw = (res == null) ? "" : res.trim().toLowerCase();
    this.cat = (ans == null || ans.isBlank()) ? "All" : ans;
    renderFilteredItems();
  }

  private void cacheAndRender(List<?> res) {
    cacheditems.clear();
    for (Object ans : res) {
      if (ans instanceof Lot) {
        cacheditems.add((Lot) ans);
      } else if (ans instanceof Item) {
        Item res1 = (Item) ans;
        Lot ans1 = new Lot();
        ans1.setId(res1.getId());
        ans1.setTitle(res1.getName());
        ans1.setDescription(res1.getDescription());
        ans1.setBidValue(res1.getCurrentPrice());
        ans1.setStartTime(res1.getStartTime());
        ans1.setEndTime(res1.getEndTime());
        ans1.setImageUrl(res1.getImageUrl());
        ans1.setSellerUsername(res1.getSellerUsername());
        ans1.setSellerAvatarUrl(res1.getSellerAvatarUrl());
        cacheditems.add(ans1);
      }
    }
    renderFilteredItems();
  }

  private void renderFilteredItems() {
    if (TrendingBind == null) return;
    TrendingBind.getChildren().clear();
    cardmap.clear();
    for (Lot ans : cacheditems) {
      if (!match(ans)) continue;
      try {
        NodeContentLoader<HBox> res = new NodeContentLoader<>();
        res.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController ans1 = res.getController();
        if (ans1 != null) {
          ans1.setData(
                  ans.getId(),
                  safe(ans.getTitle()),
                  ans.getBidValue(),
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
        Lot res1 = cacheditems.get(res);
        res1.setBidValue(ans.getCurrentPrice());
        res1.setEndTime(ans.getEndTime());
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

  private boolean match(Lot ans) {
    String res = safe(ans.getTitle()).toLowerCase();
    if (!kw.isBlank() && !res.contains(kw)) return false;
    double ans1 = KhungController.getMinPrice();
    double res1 = KhungController.getMaxPrice();
    if (res1 <= 0) res1 = Double.MAX_VALUE;
    if (ans.getBidValue() < ans1 || ans.getBidValue() > res1) return false;
    return true;
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