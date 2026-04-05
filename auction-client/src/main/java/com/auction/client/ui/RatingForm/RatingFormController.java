package com.auction.client.ui.RatingForm;

import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Rating;
import com.auction.shared.Request;
import com.auction.shared.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RatingFormController {
  @FXML private VBox RootPane;
  @FXML private Label TitleLabel;
  @FXML private HBox StarContainer;
  @FXML private TextArea FeedbackField;

  private int itemId;
  private int selectedStars = 0;
  private Label[] starLabels = new Label[5];
  private Runnable onComplete;

  @FXML
  public void initialize() {
    for (int res = 0; res < 5; res++) {
      Label ans = new Label("\u2606");
      ans.setStyle("-fx-font-size: 32; -fx-text-fill: #e2b44d; -fx-cursor: hand;");
      int res1 = res + 1;
      ans.setOnMouseClicked(e -> selectStars(res1));
      starLabels[res] = ans;
      StarContainer.getChildren().add(ans);
    }
  }

  public void setData(int itemId) {
    this.itemId = itemId;
  }

  public void setOnComplete(Runnable r) {
    this.onComplete = r;
  }

  private void selectStars(int count) {
    this.selectedStars = count;
    for (int res = 0; res < 5; res++) {
      starLabels[res].setText(res < count ? "\u2605" : "\u2606");
    }
  }

  @FXML
  private void handleCancel() {
    NodeManager.removeNodeFromPane(RootPane, KhungController.getMainContentPane());
  }

  @FXML
  private void handleSubmit() {
    if (selectedStars == 0) {
      showAlert(Alert.AlertType.WARNING, "No Rating", "Please select at least 1 star.");
      return;
    }
    String res = FeedbackField.getText();
    Rating res1 = new Rating();
    res1.setItemId(this.itemId);
    res1.setStars(this.selectedStars);
    res1.setFeedback(res == null ? "" : res.trim());

    new Thread(
            () -> {
              Request res2 = new Request(Request.SUBMIT_RATING, res1);
              Response res3 = NetworkClient.getInstance().sendRequestAndWait(res2);
              Platform.runLater(
                  () -> {
                    if (res3 != null && Response.OK.equals(res3.getStatus())) {
                      showAlert(
                          Alert.AlertType.INFORMATION,
                          "Success",
                          "Your rating has been submitted.");
                      NodeManager.removeNodeFromPane(
                          RootPane, KhungController.getMainContentPane());
                      if (onComplete != null) onComplete.run();
                    } else {
                      String ans = res3 != null ? res3.getMessage() : "Failed";
                      showAlert(Alert.AlertType.ERROR, "Error", ans);
                    }
                  });
            })
        .start();
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
    Alert res = new Alert(type);
    res.setTitle(title);
    res.setHeaderText(null);
    res.setContentText(content);
    res.showAndWait();
  }
}
