package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.ui.base.CanBind;
import com.auction.client.util.FXThread;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ReactiveFlowPane<T> extends FlowPane {
  private ObservableList<T> dataList;
  private ListChangeListener<T> activeListener;

  private Callback<T, Node> cellFactory;

  public ReactiveFlowPane() {}

  public void setAll(List<T> data, Callback<T, Node> cellFactory) {
    getChildren().clear();

    FXThread.run(() -> {
      List<Node> list = new ArrayList<>();
      for (T t : data) {
        Node node = cellFactory.call(t);
        if (node != null) {
          list.add(node);
        }
      }

      getChildren().addAll(list);
    });
  }

  public void bind(ObservableList<T> newDataList, Callback<T, Node> cellFactory) {
    unbind();

    this.dataList = newDataList;
    this.cellFactory = cellFactory;

    if (newDataList == null || cellFactory == null) return;

    this.activeListener = change -> {
      FXThread.run(() -> {
        while (change.next()) {
          if (change.wasRemoved()) {
            removeCard(change.getFrom(), change.getRemoved().size());
          }
          if (change.wasAdded()) {
            addCard(change.getAddedSubList(), change.getFrom());
          }
        }
      });
    };

    renderInitialItems();

    this.dataList.addListener(this.activeListener);
  }

  private void renderInitialItems() {
    if (dataList == null || dataList.isEmpty()) return;

    List<Node> initialNode = new ArrayList<>();
    for (T item : dataList) {
      Node node = cellFactory.call(item);
      if (node != null) {
        initialNode.add(node);
      }
    }
    getChildren().addAll(initialNode);
  }

  private void unbind() {
    if (this.dataList != null && this.activeListener != null) {
      this.dataList.removeListener(this.activeListener);
    }

    this.dataList = null;
    this.activeListener = null;
    this.cellFactory = null;
  }

  private void addCard(List<? extends T> item, int startIndex) {
    if (item == null ||  item.isEmpty()) {
      return;
    }

    List<Node> newNode =  new ArrayList<>();
    for (T t : item) {
      Node node = cellFactory.call(t);
      if (node != null) {
        newNode.add(node);
      }
    }

    int safeIndex = Math.min(startIndex, getChildren().size());
    getChildren().addAll(safeIndex, newNode);
  }

  private void removeCard(int startIndex, int count) {
    if (getChildren().isEmpty()) {
      return;
    }

    int endIndex = Math.min(startIndex + count, getChildren().size());
    if (startIndex >= endIndex) {
      return;
    }

    for (int i = startIndex; i < endIndex; i++) {
      Node node = getChildren().get(i);
      if (node instanceof CanBind canBind) {
        canBind.dispose();
      }
    }

    getChildren().remove(startIndex, endIndex);
  }
}