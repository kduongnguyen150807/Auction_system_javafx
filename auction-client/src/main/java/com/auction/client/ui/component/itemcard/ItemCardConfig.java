package com.auction.client.ui.component.itemcard;

import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.lotsinformation.ItemModel;
import javafx.scene.Node;
import javafx.util.Callback;

import java.util.function.Consumer;

public record ItemCardConfig(
  Callback<ItemModel, Node> cardFactory,
  Consumer<ItemModel> onHeartClicked,
  Consumer<ItemModel> onCardClicked
) {
  public ItemCardConfig (Consumer<ItemModel> onHeartClicked, Consumer<ItemModel> onCardClicked) {
    this(
      itemModel -> {
        ItemCard card = new ItemCard(itemModel);
        card.setOnItemClicked(onCardClicked);

        var watchSet = ClientSession.CURRENT_SESSION.getWatchedItemsList().getIdSet();
        card.setOnHeartClicked(onHeartClicked, watchSet);

        return card;
      },
      onHeartClicked,
      onCardClicked
    );
  }
}
