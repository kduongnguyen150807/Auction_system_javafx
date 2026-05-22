package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.util.FXThread;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class VBoxModel<T, U extends Node> extends VBox {

  private Function<T, Node> itemFac;
  private Consumer<T> onItemClicked;

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

  public void setOnItemClicked(Consumer<T> onItemClicked) {
    this.onItemClicked = onItemClicked;
  }

  public void setItemFac(Function<T, Node> itemFac) {
    this.itemFac = itemFac;
  }

  public void setItems(List<T> items) {
    FXThread.run(() -> {
      this.getChildren().clear();

      if (items == null ||  items.isEmpty()) {
        return;
      }

      for (T item : items) {
        Node node = itemFac.apply(item);
        node.setOnMouseClicked(event -> {
          onItemClicked.accept(item);
        });
        getChildren().add(node);
      }
    });
  }
}
