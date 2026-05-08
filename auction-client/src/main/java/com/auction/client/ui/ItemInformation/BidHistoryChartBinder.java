package com.auction.client.ui.ItemInformation;

import com.auction.shared.BidTransaction;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

/** Binds bid history and live ticks to a {@link LineChart}. */
final class BidHistoryChartBinder {

  private final LineChart<String, Number> chart;
  private final XYChart.Series<String, Number> series = new XYChart.Series<>();

  BidHistoryChartBinder(LineChart<String, Number> chart) {
    this.chart = chart;
  }

  void loadHistory(List<BidTransaction> hist, DateTimeFormatter timeFmt) {
    if (chart == null) return;
    chart.getData().clear();
    series.getData().clear();
    series.setName("Price Curve");
    for (BidTransaction b : hist)
      series
          .getData()
          .add(
              new XYChart.Data<>(
                  b.getTimestamp() != null ? b.getTimestamp().format(timeFmt) : "", b.getBidValue()));
    chart.getData().add(series);
  }

  void appendLivePrice(double price, int maxPoints) {
    if (chart == null) return;
    if (!chart.getData().contains(series)) {
      chart.getData().add(series);
      series.setName("Price Curve");
    }
    series
        .getData()
        .add(
            new XYChart.Data<>(
                java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), price));
    if (series.getData().size() > maxPoints) series.getData().remove(0);
  }
}
