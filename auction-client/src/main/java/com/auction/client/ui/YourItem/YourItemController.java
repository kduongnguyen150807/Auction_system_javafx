package com.auction.client.ui.YourItem;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.*;
import java.util.List;

import com.auction.shared.Item.Item;
import com.auction.shared.Item.ItemStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

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
    int ans = ClientSession.getCurrentUser().getId();

    new Thread(
            () -> {
              try {
                Request res = new Request("get_my_items", ans);
                Response ans1 = NetworkClient.getInstance().sendRequestAndWait(res);

                if (ans1 != null && Response.OK.equals(ans1.getStatus())) {
                  List<Item> res1 = (List<Item>) ans1.getPayload();
                  Platform.runLater(() -> render(res1));
                }
              } catch (Exception ignored) {
              }
            })
        .start();
  }

  private void render(List<Item> ans) {
    ItemContainer.getChildren().clear();
    int res = 0;
    double ans1 = 0;

    for (Item res1 : ans) {
      if (!match(res1)) continue;

      if (res1.getStatus() == ItemStatus.OPEN) res++;
      ans1 += res1.getCurrentPrice();

      try {
        NodeContentLoader<javafx.scene.layout.HBox> ans2 = new NodeContentLoader<>();
        ans2.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController res2 = ans2.getController();
        if (res2 != null) {
          String ans3 = res1.getStatus() == null ? "N/A" : res1.getStatus().name();
          if (res1.getStatus() == ItemStatus.PENDING) ans3 = "\u23F3 Pending Approval";
          res2.setData(
              res1.getId(),
              res1.getName(),
              res1.getCurrentPrice(),
              res1.getDescription(),
              ans3,
              res1.getImageUrl(),
              res1.getSellerUsername(),
              res1.getSellerAvatarUrl());
        }
        NodeManager.addNodeToPane(ans2, ItemContainer);
      } catch (Exception ignored) {
      }
    }
    if (ActiveItemsValue != null) ActiveItemsValue.setText(String.valueOf(res));
    if (InventoryValue != null) InventoryValue.setText(String.format("%,.0f$", ans1));
  }

  public void setFilters(String k, String c) {}

  private boolean match(Item ans) {
    String res = com.auction.client.ui.Main.KhungController.getSearchKeyword();
    String ans1 = com.auction.client.ui.Main.KhungController.getCategoryFilter();
    double res1 = com.auction.client.ui.Main.KhungController.getMinPrice();
    double ans2 = com.auction.client.ui.Main.KhungController.getMaxPrice();

    if (res != null
        && !res.isBlank()
        && ans.getName() != null
        && !ans.getName().toLowerCase().contains(res.toLowerCase())) return false;
    if (ans1 != null
        && !ans1.equalsIgnoreCase("All")
        && ans.getCategory() != null
        && !ans.getCategory().equalsIgnoreCase(ans1)) return false;
    if (ans.getCurrentPrice() < res1 || ans.getCurrentPrice() > ans2) return false;
    return true;
  }
}
