package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.util.FXThread;
import com.auction.shared.BidTransaction;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class BidChart extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/component/BidChart.fxml";

  private final int MAX_POINT = -1;
  private XYChart.Series<String, Number> series = new XYChart.Series<>();

  @FXML
  private LineChart priceChart;

  public BidChart() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setSelectedBidHistory(ObservableList<BidTransaction> selectedBidHistory, DateTimeFormatter formatter, int maxPoints) {
    selectedBidHistory.addListener((ListChangeListener.Change<? extends BidTransaction> change) -> {
      while (change.next()) {
        for (BidTransaction bidTransaction : change.getAddedSubList()) {
          appendLiveBid(bidTransaction, formatter, maxPoints);
        }
      }
      if (change.wasRemoved()
        || change.wasReplaced()) {
        reloadHistory(selectedBidHistory, formatter);
      }
    });
    reloadHistory(selectedBidHistory, formatter);
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
