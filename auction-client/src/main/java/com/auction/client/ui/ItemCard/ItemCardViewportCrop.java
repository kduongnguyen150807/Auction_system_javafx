package com.auction.client.ui.ItemCard;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** Center-crops an image to match the {@link ImageView} fit box. */
final class ItemCardViewportCrop {

  private ItemCardViewportCrop() {}

  static void apply(ImageView imageView, Image img) {
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
