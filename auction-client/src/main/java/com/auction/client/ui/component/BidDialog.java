package com.auction.client.ui.component;

import com.auction.client.ClientSession;
import com.auction.client.ui.base.CanCloseWindow;
import com.auction.client.ui.utils.UIutils;
import com.auction.shared.dto.BidForm;
import com.auction.shared.item.Item;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class BidDialog extends VBox implements CanCloseWindow {
  private static final String BASE_FXML_PATH = "/fxml/Component/BidDialog.fxml";

  @FXML private Label itemIdLabel;
  @FXML private TextField amountField;
  @FXML private Label announcementLabel;

  private Runnable onCloseWindow;
  private Runnable onSubmit;
  private Item item;

  @Override
  public void setCloseWindow(Runnable closeWindow) {
    this.onCloseWindow = closeWindow;
  }

  public void setOnSubmit(Runnable onSubmit) { this.onSubmit = onSubmit; }

  public BidDialog() {
    initBaseLayout();
  }

  private void initBaseLayout() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();

      /* set attribute for field */
      UIutils.setNumericField(amountField);
    } catch (IOException e) {
      throw new RuntimeException("Fail to load BidDialog",e);
    }
  }

  public void setData(Item item) {
    itemIdLabel.setText(String.valueOf(item.getId()));
  }

  public BidForm collectData(Item item) {
    if (amountField.getText().isEmpty()) {
      return null;
    }

    BidForm bidForm = new BidForm(
      item.getId(),
      ClientSession.getCurrentUser().getId(),
      Double.parseDouble(amountField.getText())
    );

    return bidForm;
  }

  @FXML
  public void handleCancel() {
    closeWindow();
  }

  @FXML
  public void handleSubmit() {
    if (onSubmit != null) {
      onSubmit.run();
    }
  }

  public void closeWindow() {
    if (onCloseWindow != null) {
      onCloseWindow.run();
    }
  }

  public void showError (String message) {
    announcementLabel.setText(message);
    announcementLabel.setVisible(true);
  }
}
