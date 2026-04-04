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
import java.util.HashMap;
import java.util.Map;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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

  public static TrangChuController getinstance() {
    TrangChuController ans = instance;
    return ans;
  }

  @FXML
  void initialize() {
    instance = this;
    setfilters(KhungController.getSearchKeyword(), KhungController.getCategoryFilter());
    refreshItems();
  }

  public void refreshItems() {
    Thread ans = new Thread(() -> {
      try {
        Request res = new Request(Request.list, null);
        Response ans1 = NetworkClient.getinstance().sendrequestandwait(res);
        if (ans1 == null || !Response.ok.equals(ans1.getstatus())) return;
        Object res1 = ans1.getpayload();
        if (!(res1 instanceof List<?> list)) return;
        Platform.runLater(() -> cacheandrender(list));
      } catch (Exception e) {}
    });
    ans.setDaemon(true);
    ans.start();
  }

  public void setfilters(String k, String c) {
    this.kw = (k == null) ? "" : k.trim().toLowerCase();
    this.cat = (c == null || c.isBlank()) ? "All" : c;
    renderfiltereditems();
  }

  private void cacheandrender(List<?> list) {
    cacheditems.clear();
    for (Object ans : list) {
      if (ans instanceof Item i) cacheditems.add(i);
    }
    renderfiltereditems();
  }

  private void renderfiltereditems() {
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
                  ans.getid(),
                  safe(ans.getname()),
                  ans.getcurrentprice(),
                  safe(ans.getdescription()),
                  formattime(ans.getendtime()),
                  safe(ans.getimageurl()),
                  safe(ans.getsellerusername()),
                  safe(ans.getselleravatarurl())
          );
          cardmap.put(ans.getid(), ans1);
        }
        NodeManager.addNodeToPane(res, TrendingBind);
      } catch (Exception e) {}
    }
  }

  public void updatepriceui(Item ans) {
    if (ans == null) return;
    updateitemprice(ans.getid(), ans.getcurrentprice());
  }

  public void updateitemprice(int ans, double res) {
    for (Item ans1 : cacheditems) {
      if (ans1.getid() == ans) {
        ans1.setcurrentprice(res);
        break;
      }
    }
    ItemCardController res1 = cardmap.get(ans);
    if (res1 != null) {
      res1.updateprice(res);
    }
  }

  private boolean match(Item ans) {
    String res = safe(ans.getname()).toLowerCase();
    if (!kw.isBlank() && !res.contains(kw)) return false;
    double ans1 = KhungController.getminprice();
    double res1 = KhungController.getmaxprice();
    if (ans.getcurrentprice() < ans1 || ans.getcurrentprice() > res1) return false;
    if ("All".equalsIgnoreCase(cat)) return true;
    return safe(ans.getcategory()).equalsIgnoreCase(cat);
  }

  private String safe(String ans) {
    String res = (ans == null) ? "" : ans;
    return res;
  }

  private String formattime(LocalDateTime ans) {
    if (ans == null) return "N/A";
    Duration res = Duration.between(LocalDateTime.now(), ans);
    if (res.isNegative() || res.isZero()) return "closed";
    long ans1 = res.toHours();
    if (ans1 / 24 > 0) return (ans1 / 24) + "d " + (ans1 % 24) + "h";
    return (ans1 % 24) + "h " + (res.toMinutes() % 60) + "m";
  }
}