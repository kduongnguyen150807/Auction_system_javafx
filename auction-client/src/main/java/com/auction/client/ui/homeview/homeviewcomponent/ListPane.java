package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.ui.base.CanBind;
import com.auction.client.util.FXThread;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListPane<T> extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/component/ListPane.fxml";

  @FXML private Label lotsName;
  @FXML private HBox listRow;
  @FXML private ScrollPane scrollPane;

  private ObservableList<T> dataList;
  private ListChangeListener<T> activeListener;

  private Callback<T, Node> cellFactory;

  public ListPane() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setTitle(String listName) {
    this.lotsName.setText(listName);
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
    listRow.getChildren().addAll(initialNode);
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

    int safeIndex = Math.min(startIndex, listRow.getChildren().size());
    listRow.getChildren().addAll(safeIndex, newNode);
  }

  private void removeCard(int startIndex, int count) {
    if (listRow.getChildren().isEmpty()) {
      return;
    }

    int endIndex = Math.min(startIndex + count, listRow.getChildren().size());
    if (startIndex >= endIndex) {
      return;
    }

    for (int i = startIndex; i < endIndex; i++) {
      Node node = listRow.getChildren().get(i);
      if (node instanceof CanBind canBind) {
        canBind.dispose();
      }
    }

    listRow.getChildren().remove(startIndex, endIndex);
  }
}
