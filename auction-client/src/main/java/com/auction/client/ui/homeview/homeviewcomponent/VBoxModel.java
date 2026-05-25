package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.ui.base.CanBind;
import com.auction.client.util.FXThread;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class VBoxModel<T> extends VBox {

  private ObservableList<T> dataList;
  private ListChangeListener<T> activeListener;

  private Callback<T, ? extends Node> cellFactory;

  public VBoxModel() {
    this.setSpacing(10.0);
    this.setMaxWidth(Double.MAX_VALUE);

    this.setStyle("""
            -fx-background-color: rgba(20, 20, 20, 0.8);
            -fx-background-radius: 10px;
            -fx-border-radius: 10px;
            -fx-padding: 15px;
            -fx-min-height: 50px;
            """);
  }

  public void bind(ObservableList<T> newDataList, Callback<T, ? extends Node> cellFactory) {
    unbind();

    this.dataList = newDataList;
    this.cellFactory = cellFactory;

    renderInitialItems();

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



    this.dataList.addListener(this.activeListener);
  }

  public void setAll(List<T> newDataList, Callback<T, ? extends Node> cellFactory) {
    unbind();
    getChildren().clear();

    List<Node> list = new ArrayList<>();
    for (T item : newDataList) {
      Node itemNode = cellFactory.call(item);
      if (itemNode != null) {
        list.add(itemNode);
      }
    }

    getChildren().addAll(list);
  }

  private void renderInitialItems() {
    getChildren().clear();
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
