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
  private final List<Item> cacheditems = new ArrayList<>();
  private String kw = "";
  private String cat = "All";

  @FXML
  void initialize() {
    setFilters(KhungController.getSearchKeyword(), KhungController.getCategoryFilter());
    refreshItems();
  }

  public void refreshItems() {
    Thread t = new Thread(() -> {
      try {
        Request req = new Request(Request.list, null);
        Response res = NetworkClient.getinstance().sendrequestandwait(req);
        if (res == null || !Response.ok.equals(res.getstatus())) return;
        Object payload = res.getpayload();
        if (!(payload instanceof List<?> list)) return;
        Platform.runLater(() -> cacheAndRender(list));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    t.setDaemon(true);
    t.start();
  }

  public void setFilters(String k, String c) {
    this.kw = (k == null) ? "" : k.trim().toLowerCase();
    this.cat = (c == null || c.isBlank()) ? "All" : c;
    renderFilteredItems();
  }

  private void cacheAndRender(List<?> list) {
    cacheditems.clear();
    for (Object o : list) {
      if (o instanceof Item i) cacheditems.add(i);
    }
    renderFilteredItems();
  }

  private void renderFilteredItems() {
    if (TrendingBind == null) return;
    TrendingBind.getChildren().clear();
    for (Item res : cacheditems) {
      if (!match(res)) continue;
      try {
        NodeContentLoader<HBox> l = new NodeContentLoader<>();
        l.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController c = l.getController();
        if (c != null) {
          c.setData(
                  res.getid(),
                  safe(res.getname()),
                  res.getcurrentprice(),
                  safe(res.getdescription()),
                  formattime(res.getendtime()),
                  safe(res.getimageurl()),
                  safe(res.getsellerusername()),
                  safe(res.getselleravatarurl())
          );
        }
        NodeManager.addNodeToPane(l, TrendingBind);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private boolean match(Item i) {
    String n = safe(i.getname()).toLowerCase();
    if (!kw.isBlank() && !n.contains(kw)) return false;
    if ("All".equalsIgnoreCase(cat)) return true;
    return safe(i.getcategory()).equalsIgnoreCase(cat);
  }

  private String safe(String s) {
    String ans = (s == null) ? "" : s;
    return ans;
  }

  private String formattime(LocalDateTime t) {
    if (t == null) return "N/A";
    Duration d = Duration.between(LocalDateTime.now(), t);
    if (d.isNegative() || d.isZero()) return "closed";
    long h = d.toHours();
    if (h / 24 > 0) return (h / 24) + "d " + (h % 24) + "h";
    return (h % 24) + "h " + (d.toMinutes() % 60) + "m";
  }
}