package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.ui.base.CanCloseWindow;
import com.auction.client.ui.component.IntegerField;
import com.auction.client.util.StringFormat;
import com.auction.shared.Item;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class BiddingForm extends StackPane implements CanCloseWindow {
  private static final String BASE_FXML_PATH = "/fxml/component/BiddingForm.fxml";

  @FXML private Label itemId;
  @FXML private Label itemName;
  @FXML private Label maxPriceInfo;
  @FXML private IntegerField bidAmount;

  private Runnable closeWindow;
  private Runnable onConfirm;

  public BiddingForm(Item item) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
      setData(item);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setData(Item item) {
    itemId.setText(String.valueOf(item.getId()));
    itemName.setText(item.getName());
    maxPriceInfo.setText(StringFormat.formatMoney(item.getMaxPrice()));
  }

  @FXML
  private void handleConfirm() {
    if (onConfirm != null) {
      onConfirm.run();
    }
    if (closeWindow != null) {
      closeWindow.run();
    }
  }

  @FXML
  private void handleCancel() {
    if (closeWindow != null) {
      closeWindow.run();
    }
  }

  @Override
  public void setCloseWindow(Runnable closeWindow) {
    this.closeWindow = closeWindow;
  }

  public void setOnConfirm(Runnable onConfirm) {
    this.onConfirm = onConfirm;
  }

  public double collectData() {
    double amount = bidAmount.getValue();
    return amount;
  }
}