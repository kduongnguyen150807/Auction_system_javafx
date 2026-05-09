package com.auction.client.ui.SearchBar;

import com.auction.shared.User;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;

/**
 * Floating user search results under the search field. Keeps {@link ThanhTimKiemController} focused
 * on wiring and item filters.
 */
final class UserSearchResultsPopup {

  private final Popup popup = new Popup();
  private final VBox resultsBox = new VBox();

  UserSearchResultsPopup() {
    popup.setAutoHide(true);
    resultsBox.setStyle(
        "-fx-background-color: #1a1a2e; -fx-background-radius: 10; -fx-border-color: #333;"
            + " -fx-border-radius: 10; -fx-border-width: 1;"
            + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0.3, 0, 4);");
    resultsBox.setPrefWidth(500);
    resultsBox.setMaxHeight(400);
    popup.getContent().add(resultsBox);
  }

  void showUnder(TextField searchField, List<User> users, Consumer<User> onUserChosen) {
    resultsBox.getChildren().clear();
    ScrollPane scroll = new ScrollPane();
    scroll.setFitToWidth(true);
    scroll.setMaxHeight(380);
    scroll.setPrefWidth(500);
    scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
    VBox content = new VBox(0);
    content.setStyle("-fx-background-color: #1a1a2e;");
    if (users == null || users.isEmpty()) {
      Label empty = new Label("No users found");
      empty.setStyle("-fx-text-fill: #888; -fx-font-size: 14; -fx-padding: 16;");
      content.getChildren().add(empty);
    } else {
      for (User u : users) {
        content.getChildren().add(buildUserRow(u, onUserChosen));
      }
    }
    scroll.setContent(content);
    resultsBox.getChildren().add(scroll);
    Bounds b = searchField.localToScreen(searchField.getBoundsInLocal());
    if (b != null) {
      popup.show(searchField.getScene().getWindow(), b.getMinX(), b.getMaxY() + 4);
    }
  }

  private static HBox buildUserRow(User u, Consumer<User> onUserChosen) {
    HBox row = new HBox(12);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(10, 14, 10, 14));
    row.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
    row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #252540; -fx-cursor: hand;"));
    row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

    ImageView avatar = new ImageView();
    avatar.setFitWidth(36);
    avatar.setFitHeight(36);
    avatar.setPreserveRatio(false);
    avatar.setClip(new Circle(18, 18, 18));
    String avatarUrl = u.getAvatarUrl();
    if (avatarUrl != null && !avatarUrl.isBlank()) {
      avatar.setImage(new Image(avatarUrl, 36, 36, true, true, true));
    }

    Label nameLabel = new Label(u.getUsername());
    nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
    String fn = u.getFullName();
    Label subLabel =
        new Label(
            fn != null && !fn.isBlank() && !fn.equals(u.getUsername())
                ? fn
                : (u.getEmail() != null ? u.getEmail() : ""));
    subLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
    VBox info = new VBox(2, nameLabel, subLabel);
    HBox.setHgrow(info, Priority.ALWAYS);

    VBox ratingBox = new VBox(0);
    ratingBox.setAlignment(Pos.CENTER_RIGHT);
    if (u.getTotalRatings() > 0) {
      int stars = Math.max(0, Math.min((int) Math.round(u.getAvgRating()), 5));
      String color = u.getAvgRating() <= 2.0 ? "#ff4444" : (u.getAvgRating() <= 3.0 ? "#ffaa00" : "#44cc44");
      Label starsLbl = new Label("\u2605".repeat(stars));
      starsLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13;");
      Label countLbl = new Label(String.format("%.1f (%d)", u.getAvgRating(), u.getTotalRatings()));
      countLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11;");
      ratingBox.getChildren().addAll(starsLbl, countLbl);
    } else {
      Label noRating = new Label("No ratings");
      noRating.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
      ratingBox.getChildren().add(noRating);
    }
    row.getChildren().addAll(avatar, info, ratingBox);
    row.setOnMouseClicked(
        e -> {
          if (onUserChosen != null) {
            onUserChosen.accept(u);
          }
        });
    return row;
  }

  void hide() {
    if (popup.isShowing()) {
      popup.hide();
    }
  }

  boolean isShowing() {
    return popup.isShowing();
  }
}
