package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.store.SelectedItem;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.util.FXThread;
import com.auction.shared.Item;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;

public class ListPane extends VBox implements CanSwitchNode<HomeViewType> {
  private static final String BASE_FXML_PATH = "/fxml/component/ListPane.fxml";

  @FXML private Label lotsName;
  @FXML private HBox listRow;

  private FilteredList<Item> filteredList;

  private Consumer<HomeViewType> switchNode;

  private String ITEM_TYPE;

  public ListPane() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    initListener();
  }

  public void setData(String lotsName, String ItemType) {
    this.lotsName.setText(lotsName);
    this.ITEM_TYPE = ItemType;
  }

  private void initListener() {
    if (filteredList == null) return;

    filteredList.addListener((ListChangeListener<Item>) change -> {
      FXThread.run(this::renderItem);
    });
  }

  @FXML
  private void handlePrev() {

  }

  @FXML
  private void handleNext() {

  }

  public void setItems(FilteredList<Item> filteredList) {
    this.filteredList = filteredList;
    renderItem();
    filteredList.addListener((ListChangeListener<Item>) change -> {
      FXThread.run(this::renderItem);
    });
  }

  public void renderItem() {
    listRow.getChildren().clear();

    for (Item item : filteredList) {
      ItemCard itemCard = new ItemCard(item);
      listRow.getChildren().add(itemCard);

      registerItemCard(itemCard);
    }
  }

  private void registerItemCard(ItemCard itemCard) {
    itemCard.setOnMouseClicked(event -> {
      SelectedItem.SELECTED_ITEM.setSelectedItem(itemCard.getItem());
      if (switchNode != null) {
        switchNode.accept(HomeViewType.ITEM_INFORMATION);
      } else {
        System.out.println("switchNode is null");
      }
    });
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}
