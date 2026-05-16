package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.store.ClientItem;
import com.auction.client.store.SelectedItem;
import com.auction.client.store.SelectedItemBidHistory;
import com.auction.client.util.FXThread;
import com.auction.client.util.ImageViewUtils;
import com.auction.client.util.TimeFormat;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.ImageView;

import java.time.format.DateTimeFormatter;

public class ItemInformationController {
  @FXML private InfoLayoutController infoLayoutController;
  @FXML private ButtonLayoutController buttonLayoutController;
  @FXML private LineChart priceChart;


  @FXML private ImageView itemImageHolder;

  private XYChart.Series<String, Number> series = new XYChart.Series<>();

  @FXML
  public void initialize() {
    SelectedItem.SELECTED_ITEM.selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> {
        applyItem(newValue);
      });
    applyItem(SelectedItem.SELECTED_ITEM.getSelectedItem());

    SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY.selectedItemBidHistoryProperty()
      .addListener((ListChangeListener.Change<? extends BidTransaction> change) -> {
        while (change.next()) {
          for (BidTransaction bidTransaction : change.getAddedSubList()) {
            appendLiveBid(bidTransaction, TimeFormat.DMY_HM, SelectedItemBidHistory.MAX_POINTS);
          }
        }
        if (change.wasRemoved()
          || change.wasReplaced()) {
          reloadHistory(SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY.selectedItemBidHistoryProperty(), TimeFormat.DMY_HM);
        }
      });
  }

  private void applyItem(ClientItem item) {
    if (item == null) {
      System.out.println("null");
      return;
    }
    infoLayoutController.setSelectedItem(item);
    buttonLayoutController.setSelectedItem(item);

    loadImage(item.getItem());

  }

  private void loadImage(Item item) {
    if  (itemImageHolder == null) {
      return;
    }

    if (item.getImageUrl().isBlank() || item.getImageUrl() == null) {
      itemImageHolder.setImage(null);
    }

    ImageViewUtils.setImageToImageView(itemImageHolder, item.getImageUrl());
  }

  private void appendLiveBid(BidTransaction bidTransaction, DateTimeFormatter timeFormatter, int maxPoints) {
    if (bidTransaction == null) {
      return;
    }

    String time = bidTransaction.getTimestamp().format(timeFormatter);
    FXThread.run(() -> {
      if (!priceChart.getData().contains(series)) {
        priceChart.getData().add(series);
      }

      series.getData().add(new XYChart.Data<>(time, bidTransaction.getBidValue()));

      if (series.getData().size() > maxPoints) {
        series.getData().removeFirst();
      }
    });
  }

  private void reloadHistory(ObservableList<BidTransaction> history, DateTimeFormatter timeFormatter) {
    if (priceChart == null) {
      return;
    }

    FXThread.run(() -> {
      series.getData().clear();

      for (BidTransaction bidTransaction : history) {
        String time = bidTransaction.getTimestamp().format(timeFormatter);
        series.getData().add(new XYChart.Data<>(time, bidTransaction.getBidValue()));
      }
      if (!priceChart.getData().contains(series)) {
        priceChart.getData().add(series);
      }

      series.setName("Price Curve");
    });
  }
}
