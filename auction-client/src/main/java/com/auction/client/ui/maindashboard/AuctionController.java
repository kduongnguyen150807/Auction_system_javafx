package com.auction.client.ui.maindashboard;

import com.auction.client.service.AuctionService;
import com.auction.client.service.AuthService;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.PageController;
import com.auction.client.ui.component.ItemCard;
import com.auction.client.ui.component.ItemListPane;
import com.auction.client.ui.utils.FXThread;
import com.auction.shared.item.ItemStatus;
import javafx.fxml.FXML;
import javafx.scene.Node;


public class AuctionController extends PageController implements CanRefresh {
  @FXML
  private ItemListPane trendingBind;

  @FXML
  public void initialize() {
    refreshData();
  }

  @Override
  public void refreshData() {
    AuctionService.getInstance().refreshItems().thenAccept(
      items -> FXThread.dispatch(this::renderItemCards)
    );
  }

  private void registerItemCard(Node node) {
    node.setOnMouseClicked(event -> {
      if (node instanceof ItemCard ic) {
        AuctionService.getInstance().setFocusedItem(ic.getItem());
      }
      switchView.accept(HomeViewType.ITEM_INFORMATION);
    });
  }

  private void renderItemCards() {
    trendingBind.setItems(AuctionService.getInstance().getItemsByStatus(ItemStatus.OPEN));

    for (Node node: trendingBind.getChildren()) {
      registerItemCard(node);
    }
  }
}
