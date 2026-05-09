package com.auction.client.util;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

/** Small helpers for loading remote images into JavaFX views (URLs, circular crops). */
public final class ImagePresentationUtil {

  private ImagePresentationUtil() {}

  public static String safeImageUrl(String url) {
    return url != null && url.contains(".webp") ? url.replace(".webp", ".jpg") : url;
  }

  /**
   * Center-crops to a square and clips as a circle. Does not change {@link ImageView} fit size — rely on FXML/CSS sizing.
   */
  public static void loadCircularAvatar(ImageView view, String url, double radius) {
    loadCircularAvatar(view, url, radius, 0);
  }

  /**
   * Same as {@link #loadCircularAvatar(ImageView, String, double)} but sets square {@code fitWidth}/{@code fitHeight}
   * to {@code displayEdgeLength} when {@code displayEdgeLength > 0}.
   */
  public static void loadCircularAvatar(ImageView view, String url, double radius, double displayEdgeLength) {
    if (view == null || url == null || url.isBlank()) return;
    String u = safeImageUrl(url);
    Image img = new Image(u, true);
    img.progressProperty()
        .addListener(
            (obs, oldv, newv) -> {
              if (newv.doubleValue() == 1.0 && !img.isError())
                Platform.runLater(() -> applyCircularCrop(view, img, radius, displayEdgeLength));
            });
    if (!img.isError()) view.setImage(img);
  }

  private static void applyCircularCrop(ImageView view, Image img, double radius, double displayEdgeLength) {
    double w = img.getWidth(), h = img.getHeight(), side = Math.min(w, h);
    view.setViewport(new Rectangle2D((w - side) / 2, (h - side) / 2, side, side));
    view.setImage(img);
    if (displayEdgeLength > 0) {
      view.setFitWidth(displayEdgeLength);
      view.setFitHeight(displayEdgeLength);
      view.setPreserveRatio(false);
    }
    view.setClip(new Circle(radius, radius, radius));
  }
}
