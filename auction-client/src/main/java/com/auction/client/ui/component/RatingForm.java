package com.auction.client.ui.component;

import com.auction.client.ui.base.CanCloseWindow;
import com.auction.shared.Rating;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

import static com.auction.client.util.AlertUtil.showAlert;

public class RatingForm extends VBox implements CanCloseWindow {
  public static final String BASE_FXML_PATH = "/fxml/component/RatingForm.fxml";

  private Label[] starLabels = new Label[5];
  private int selectedStars = 0;

  private int itemid;

  private Runnable onCloseWindow;
  private Runnable onSubmit;

  @FXML private HBox starContainer;
  @FXML private TextArea feedbackField;

  public RatingForm(int id) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
      this.itemid = id;
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  @FXML
  public void initialize() {
    for (int starIndex = 0; starIndex < 5; starIndex++) {
      Label star = new Label("\u2606");
      star.setStyle("-fx-font-size: 32; -fx-text-fill: #e2b44d; -fx-cursor: hand;");
      int ratingValue = starIndex + 1;
      star.setOnMouseClicked(mouseEvent -> selectStars(ratingValue));
      starLabels[starIndex] = star;
      starContainer.getChildren().add(star);
    }
  }

  private void selectStars(int count) {
    this.selectedStars = count;
    for (int i = 0; i < 5; i++) {
      starLabels[i].setText(i < count ? "\u2605" : "\u2606");
    }
  }

  public Rating collectData() {
    if (selectedStars == 0) {
      return null;
    }
    String feedback = feedbackField.getText();
    Rating rating = new Rating();
    rating.setItemId(itemid);
    rating.setStars(selectedStars);
    rating.setFeedback(feedback == null ? "" : feedback.trim());
    return rating;
  }

  @FXML
  public void handleCancel() {
    if (onCloseWindow != null) {
      onCloseWindow.run();
    }
  }

  @FXML
  public void handleSubmit() {
    if (onSubmit != null) {
      onSubmit.run();
    }

    if (onCloseWindow != null) {
      onCloseWindow.run();
    }
  }

  public void setOnSubmit(Runnable onSubmit) {
    this.onSubmit = onSubmit;
  }

  @Override
  public void setCloseWindow(Runnable closeWindow) {
    this.onCloseWindow = closeWindow;
  }
}
