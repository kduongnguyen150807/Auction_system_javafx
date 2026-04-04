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

  public void refreshItems() {
    if (ClientSession.getCurrentUser() == null) return;
    int res_uid = ClientSession.getCurrentUser().getid();

    new Thread(() -> {
      try {
        Request req = new Request("get_my_items", res_uid);
        Response res = NetworkClient.getinstance().sendrequestandwait(req);

        if (res != null && Response.ok.equals(res.getstatus())) {
          List<Item> ans = (List<Item>) res.getpayload();
          Platform.runLater(() -> render(ans));
        }
      } catch (Exception ignored) {}
    }).start();
  }

  private void render(List<Item> items) {
    ItemContainer.getChildren().clear();
    int res_active = 0;
    double res_total = 0;

    for (Item res_i : items) {
      if (res_i.getstatus() == ItemStatus.OPEN) res_active++;
      res_total += res_i.getcurrentprice();

      try {
        NodeContentLoader<javafx.scene.layout.HBox> res_l = new NodeContentLoader<>();
        res_l.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController res_c = res_l.getController();
        if (res_c != null) {
          String res_status = res_i.getstatus() == null ? "N/A" : res_i.getstatus().name();
          if (res_i.getstatus() == ItemStatus.PENDING) res_status = "\u23F3 Pending Approval";
          res_c.setData(res_i.getid(), res_i.getname(), res_i.getcurrentprice(), res_i.getdescription(),
                  res_status,
                  res_i.getimageurl(), res_i.getsellerusername(), res_i.getselleravatarurl());
        }
        NodeManager.addNodeToPane(res_l, ItemContainer);
      } catch (Exception ignored) {}
    }
    ActiveItemsValue.setText(String.valueOf(res_active));
    InventoryValue.setText(String.format("%,.0f$", res_total));
  }

  public void setFilters(String k, String c) {}
}