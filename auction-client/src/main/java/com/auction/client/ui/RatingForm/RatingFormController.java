package com.auction.client.ui.RatingForm;

import com.auction.client.app.NodeManager;
import com.auction.client.service.BiddingClientService;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Rating;
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
  @FXML private HBox StarContainer;
  @FXML private TextArea FeedbackField;

  private int itemId;
  private int selectedStars = 0;
  private Label[] starLabels = new Label[5];
  private Runnable onComplete;
  private final BiddingClientService biddingClientService = new BiddingClientService();

  @FXML
  public void initialize() {
    for (int starIndex = 0; starIndex < 5; starIndex++) {
      Label star = new Label("\u2606");
      star.setStyle("-fx-font-size: 32; -fx-text-fill: #e2b44d; -fx-cursor: hand;");
      int ratingValue = starIndex + 1;
      star.setOnMouseClicked(mouseEvent -> selectStars(ratingValue));
      starLabels[starIndex] = star;
      StarContainer.getChildren().add(star);
    }
  }

  public void setData(int itemId) {
    this.itemId = itemId;
  }

  public void setOnComplete(Runnable onCompleteCallback) {
    this.onComplete = onCompleteCallback;
  }

  private void selectStars(int count) {
    this.selectedStars = count;
    for (int i = 0; i < 5; i++) {
      starLabels[i].setText(i < count ? "\u2605" : "\u2606");
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
    String feedbackText = FeedbackField.getText();
    Rating rating = new Rating();
    rating.setItemId(this.itemId);
    rating.setStars(this.selectedStars);
    rating.setFeedback(feedbackText == null ? "" : feedbackText.trim());

    new Thread(
            () -> {
              Response response = biddingClientService.submitRating(rating);
              Platform.runLater(
                  () -> {
                    if (response != null && Response.OK.equals(response.getStatus())) {
                      showAlert(
                          Alert.AlertType.INFORMATION,
                          "Success",
                          "Your rating has been submitted.");
                      NodeManager.removeNodeFromPane(
                          RootPane, KhungController.getMainContentPane());
                      if (onComplete != null) onComplete.run();
                    } else {
                      String errorMessage = response != null ? response.getMessage() : "Failed";
                      showAlert(Alert.AlertType.ERROR, "Error", errorMessage);
                    }
                  });
            })
        .start();
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
