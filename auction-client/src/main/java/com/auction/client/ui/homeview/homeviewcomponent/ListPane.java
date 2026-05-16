package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.util.FXThread;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

public class ListPane<T> extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/component/ListPane.fxml";

  @FXML private Label lotsName;
  @FXML private HBox listRow;

  private FilteredList<T> filteredList;

  private Function<T, Node> cardFactory;

  private Consumer<T> onItemClicked;

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

  @FXML
  private void handlePrev() {

  }

  @FXML
  private void handleNext() {

  }

  @SuppressWarnings("unchecked")
  public void setItems(
    FilteredList<T> filteredList,
    Function<T, Node> cardFactory,
    Consumer<T> onItemClicked
  ) {
    this.filteredList = filteredList;

    this.cardFactory = cardFactory;

    this.onItemClicked = onItemClicked;

    this.filteredList.addListener((ListChangeListener<T> ) change ->
        FXThread.run(this::renderItems)
    );

    renderItems();
  }

  public void renderItems() {
    if (filteredList == null) return;

    listRow.getChildren().clear();

    for (T item : filteredList) {
      Node node = cardFactory.apply(item);
      node.setOnMouseClicked(event -> {
        if (onItemClicked != null) {
          onItemClicked.accept(item);
        }
      });
      listRow.getChildren().add(node);
    }
  }
}
