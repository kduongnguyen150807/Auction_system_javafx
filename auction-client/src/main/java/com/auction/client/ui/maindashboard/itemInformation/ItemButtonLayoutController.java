package com.auction.client.ui.maindashboard.itemInformation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ItemButtonLayoutController {

  private Runnable onBid;

  public void setOnBid(Runnable onBid) {
    this.onBid = onBid;
  }

  @FXML
  public void handleBid() {
    if (onBid != null) {
      onBid.run();
    }
  }

  @FXML
  public void handleAutoBid(ActionEvent actionEvent) {
  }

  @FXML
  public void handleRate(ActionEvent actionEvent) {
  }
}
