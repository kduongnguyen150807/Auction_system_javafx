package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.util.FXThread;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ReactiveFlowPane extends FlowPane {
  private ObservableList<ItemModel> itemList;
  private ListChangeListener<ItemModel> activeListener;
  private Consumer<ItemModel> onCardClicked;

  public ReactiveFlowPane() {}

  public void bindList(ObservableList<ItemModel> newItemList, Consumer<ItemModel> onCardClicked) {
    unbind();
    this.onCardClicked = onCardClicked;
    this.itemList = newItemList;
    if (newItemList == null) { return; }

    renderInitialItems();

    this.activeListener = change -> FXThread.run(() -> {
      while (change.next()) {
        if (change.wasRemoved()) {
          List<? extends ItemModel> removedModels = change.getRemoved();
          removeCards(removedModels);
        }
        if (change.wasAdded()) {
          List<? extends ItemModel> addedModels = change.getAddedSubList();
          int startIndex = change.getFrom();
          addCards(addedModels, startIndex);
        }
      }
    });

    this.itemList.addListener(this.activeListener);
  }

  private void unbind() {
    this.onCardClicked = null;
    if (this.itemList != null) {
      this.itemList.removeListener(this.activeListener);
      this.itemList = null;
    }
    getChildren().clear();
  }

  private void renderInitialItems() {
    getChildren().clear();
    if (itemList == null || itemList.isEmpty()) {
      return;
    }
    for (ItemModel item : itemList) {
      ItemCard itemCard = new ItemCard(item);
      itemCard.setOnMouseClicked(event -> {
        if (onCardClicked != null) {
          onCardClicked.accept(itemCard.getItemModel());
        }
      });
      getChildren().add(itemCard);
    }
  }

  private void removeCards(List<? extends ItemModel> itemsToBeRemoved) {
    if (itemsToBeRemoved == null || itemsToBeRemoved.isEmpty() || getChildren().isEmpty()) {
      return;
    }

    getChildren().removeIf(node -> {
      if (node instanceof ItemCard card) {
        if (itemsToBeRemoved.contains(card.getItemModel())) {
          card.dispose();
          return true;
        }
      }
      return false;
    });
  }

  private void addCards(List<? extends ItemModel> items, int startIndex) {
    if (items == null || items.isEmpty()) return;

    List<ItemCard> newCards = new ArrayList<>();
    for (ItemModel item : items) {
      ItemCard newCard = new ItemCard(item);
      newCard.setOnMouseClicked(event -> {
        if (onCardClicked != null) {
          onCardClicked.accept(newCard.getItemModel());
        }
      });
      newCards.add(newCard);
    }

    int safeIndex = Math.min(startIndex, getChildren().size());
    getChildren().addAll(safeIndex, newCards);
  }
}