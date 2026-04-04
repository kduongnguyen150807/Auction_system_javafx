package com.auction.client.ui.YourItem;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import java.util.List;

public class YourItemController {
  @FXML private FlowPane ItemContainer;
  @FXML private Label ActiveItemsValue, InventoryValue;

  @FXML
  void initialize() {
    refreshItems();
  }

  @FXML
  public void refreshItems() {
    if (ClientSession.getCurrentUser() == null) return;
    int ans = ClientSession.getCurrentUser().getid();

    new Thread(() -> {
      try {
        Request res = new Request("get_my_items", ans);
        Response ans1 = NetworkClient.getinstance().sendrequestandwait(res);

        if (ans1 != null && Response.ok.equals(ans1.getstatus())) {
          List<Item> res1 = (List<Item>) ans1.getpayload();
          Platform.runLater(() -> render(res1));
        }
      } catch (Exception ignored) {}
    }).start();
  }

  private void render(List<Item> ans) {
    ItemContainer.getChildren().clear();
    int res = 0;
    double ans1 = 0;

    for (Item res1 : ans) {
      if (!match(res1)) continue;

      if (res1.getstatus() == ItemStatus.OPEN) res++;
      ans1 += res1.getcurrentprice();

      try {
        NodeContentLoader<javafx.scene.layout.HBox> ans2 = new NodeContentLoader<>();
        ans2.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController res2 = ans2.getController();
        if (res2 != null) {
          String ans3 = res1.getstatus() == null ? "N/A" : res1.getstatus().name();
          if (res1.getstatus() == ItemStatus.PENDING) ans3 = "\u23F3 Pending Approval";
          res2.setData(res1.getid(), res1.getname(), res1.getcurrentprice(), res1.getdescription(),
                  ans3,
                  res1.getimageurl(), res1.getsellerusername(), res1.getselleravatarurl());
        }
        NodeManager.addNodeToPane(ans2, ItemContainer);
      } catch (Exception ignored) {}
    }
    if (ActiveItemsValue != null) ActiveItemsValue.setText(String.valueOf(res));
    if (InventoryValue != null) InventoryValue.setText(String.format("%,.0f$", ans1));
  }

  public void setfilters(String k, String c) {}

  private boolean match(Item ans) {
    String res = com.auction.client.ui.Main.KhungController.getSearchKeyword();
    String ans1 = com.auction.client.ui.Main.KhungController.getCategoryFilter();
    double res1 = com.auction.client.ui.Main.KhungController.getminprice();
    double ans2 = com.auction.client.ui.Main.KhungController.getmaxprice();

    if (res != null && !res.isBlank() && ans.getname() != null && !ans.getname().toLowerCase().contains(res.toLowerCase())) return false;
    if (ans1 != null && !ans1.equalsIgnoreCase("All") && ans.getcategory() != null && !ans.getcategory().equalsIgnoreCase(ans1)) return false;
    if (ans.getcurrentprice() < res1 || ans.getcurrentprice() > ans2) return false;
    return true;
  }
}