package com.auction.client.ui.ItemInformation;

import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

final class BidHistoryChartBinder {
  private final LineChart<String, Number> chart;
  private final XYChart.Series<String, Number> series = new XYChart.Series<>();

  BidHistoryChartBinder(LineChart<String, Number> chart) {
    this.chart = chart;
  }

  void loadHistory(Item item, List<BidTransaction> hist, DateTimeFormatter timefmt) {
    if (chart == null) {
      return;
    }
    if (!chart.getData().contains(series)) {
      chart.getData().add(series);
    }
    series.setName("Price Curve");
    series.getData().clear();
    if (item != null) {
      String starttime = item.getStartTime() != null ? item.getStartTime().format(timefmt) : "";
      series.getData().add(new XYChart.Data<>(starttime, item.getStartingPrice()));
    }
    for (BidTransaction b : hist) {
      String t = b.getTimestamp() != null ? b.getTimestamp().format(timefmt) : "";
      series.getData().add(new XYChart.Data<>(t, b.getBidValue()));
    }
  }

  void appendLivePrice(double price, int maxpoints) {
    if (chart == null) {
      return;
    }
    if (!chart.getData().contains(series)) {
      chart.getData().add(series);
      series.setName("Price Curve");
    }
    String now = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    series.getData().add(new XYChart.Data<>(now, price));
    if (series.getData().size() > maxpoints) {
      series.getData().remove(0);
    }
  }
}