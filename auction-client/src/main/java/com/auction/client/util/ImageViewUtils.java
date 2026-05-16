package com.auction.client.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageViewUtils {
  private ImageViewUtils() {}

  public static void setImageToImageView(ImageView imageView, String imageUrl) {
    Image img = new Image(imageUrl, true);
    img.progressProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue.doubleValue() == 1) {
        FXThread.run(() -> {
          ImageViewUtils.apply(imageView, img);
        });
      }
    });
    imageView.setImage(img);
  }

  public static void apply(ImageView imageView, Image img) {
    if (imageView == null || img == null) {
      return;
    }
    double w = img.getWidth();
    double h = img.getHeight();
    double targetW = imageView.getFitWidth();
    double targetH = imageView.getFitHeight();

    double imgRatio = w / h;
    double targetRatio = targetW / targetH;

    double cropW;
    double cropH;
    double cropX;
    double cropY;
    if (imgRatio > targetRatio) {
      cropH = h;
      cropW = h * targetRatio;
      cropX = (w - cropW) / 2;
      cropY = 0;
    } else {
      cropW = w;
      cropH = w / targetRatio;
      cropX = 0;
      cropY = (h - cropH) / 2;
    }

    imageView.setViewport(new Rectangle2D(cropX, cropY, cropW, cropH));
  }
}
