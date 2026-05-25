package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.shared.Rating;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Builds rating rows inside a {@link VBox} from cached {@link Rating}s and a sentiment filter. */
final class RatingListRenderer {

  private RatingListRenderer() {}

  static void render(VBox ratingsContainer, List<Rating> cachedRatings, String filter) {
    if (ratingsContainer == null) return;
    ratingsContainer.getChildren().removeIf(n -> !(n instanceof HBox));
    boolean any = false;
    for (Rating r : cachedRatings) {
      String s = r.getStars() <= 2 ? "Negative" : (r.getStars() == 3 ? "Neutral" : "Positive");
      if (!"All".equals(filter) && !s.equals(filter)) continue;
      any = true;
      String color = r.getStars() <= 2 ? "#ff4444" : (r.getStars() == 3 ? "#ffaa00" : "#44ff44");
      Label hdr =
        new Label(
          (r.getRaterUsername() != null ? r.getRaterUsername() : "User")
            + ": "
            + "\u2605".repeat(r.getStars())
            + "\u2606".repeat(5 - r.getStars())
            + "  ["
            + s
            + "]");
      hdr.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13;");
      ratingsContainer.getChildren().add(hdr);
      if (r.getFeedback() != null && !r.getFeedback().isBlank()) {
        Label fb = new Label("  \"" + r.getFeedback() + "\"");
        fb.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12; -fx-font-style: italic;");
        fb.setWrapText(true);
        ratingsContainer.getChildren().add(fb);
      }
    }
    if (!any && !"All".equals(filter)) {
      Label e = new Label("No " + filter.toLowerCase() + " ratings.");
      e.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
      ratingsContainer.getChildren().add(e);
    }
    ratingsContainer.setVisible(!cachedRatings.isEmpty());
    ratingsContainer.setManaged(!cachedRatings.isEmpty());
  }
}