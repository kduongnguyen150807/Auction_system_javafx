package com.auction.client.ui.ItemCard;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.AuctionType;
import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Item;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class ItemCardController {
  private static final double FULL_IMG_W = 270;
  private static final double FULL_IMG_H = 240;
  /** Category row: fixed “slot” width in parent HBox (image + padding). */
  private static final double COMPACT_IMG_W = 180;
  private static final double COMPACT_IMG_H = 158;
  public static final double CATEGORY_SLOT_WIDTH = 208;

  @FXML private VBox itemRoot;
  @FXML private Label ItemName, ItemDescription, Price, TimeRemain;
  @FXML private Label priceMetricCaption;
  @FXML private ImageView ImageHolder;
  @FXML private Rectangle imageClip;

  private int id;
  private String itemName, description, timeLabel, imageUrl, sellerName, sellerAvatarUrl;
  private double currentPrice;
  private LocalDateTime endTime;
  /** Live catalog snapshot for Dutch timers + labels */
  private Item catalogItemSnapshot;

  public void attachCatalogItem(Item catalogItem) {
    this.catalogItemSnapshot = catalogItem;
    refreshPriceMetricCaption();
  }

  /** Home category rows: smaller image + fixed card width (one slot per cell, no horizontal grow). */
  public void setCompactRowLayout(boolean compact) {
    if (itemRoot != null) {
      itemRoot.getStyleClass().remove("item-card-compact");
      if (compact) itemRoot.getStyleClass().add("item-card-compact");
      if (compact) {
        itemRoot.setMinWidth(CATEGORY_SLOT_WIDTH);
        itemRoot.setPrefWidth(CATEGORY_SLOT_WIDTH);
        itemRoot.setMaxWidth(CATEGORY_SLOT_WIDTH);
      } else {
        itemRoot.setMinWidth(Region.USE_COMPUTED_SIZE);
        itemRoot.setPrefWidth(Region.USE_COMPUTED_SIZE);
        itemRoot.setMaxWidth(Double.MAX_VALUE);
      }
    }
    double w = compact ? COMPACT_IMG_W : FULL_IMG_W;
    double h = compact ? COMPACT_IMG_H : FULL_IMG_H;
    if (ImageHolder != null) {
      ImageHolder.setFitWidth(w);
      ImageHolder.setFitHeight(h);
    }
    if (imageClip != null) {
      imageClip.setWidth(w);
      imageClip.setHeight(h);
      imageClip.setArcWidth(compact ? 20 : 30);
      imageClip.setArcHeight(compact ? 20 : 30);
    }
    if (ImageHolder != null && ImageHolder.getImage() != null) {
      applyCenterCrop(ImageHolder, ImageHolder.getImage());
    }
  }

  public VBox getRootNode() {
    return itemRoot;
  }

  public void setData(
      int itemId,
      String name,
      double price,
      String desc,
      String timeLabel,
      String imageUrl,
      String sellerName,
      String sellerAvatarUrl) {
    this.id = itemId;
    this.itemName = name;
    this.currentPrice = price;
    this.description = desc;
    this.timeLabel = timeLabel;
    this.imageUrl = imageUrl;
    this.sellerName = sellerName;
    this.sellerAvatarUrl = sellerAvatarUrl;

    if (ItemName != null) ItemName.setText(this.itemName);
    if (ItemDescription != null) ItemDescription.setText(this.description);
    if (Price != null) Price.setText(String.format("%,.0f$", this.currentPrice));
    if (TimeRemain != null) TimeRemain.setText(this.timeLabel);

    loadImageIfPresent(this.imageUrl);
    refreshPriceMetricCaption();
  }

  private void refreshPriceMetricCaption() {
    if (priceMetricCaption == null) return;
    boolean dutch =
        catalogItemSnapshot != null
            && catalogItemSnapshot.getAuctionType() == AuctionType.DUTCH;
    priceMetricCaption.setText(dutch ? "CURRENT PRICE" : "CURRENT BID");
  }

  private void loadImageIfPresent(String url) {
    if (ImageHolder == null || url == null || url.isBlank()) return;
    Image img = new Image(url, true);
    img.progressProperty()
        .addListener(
            (obs, oldv, newv) -> {
              if (newv.doubleValue() == 1.0) {
                Platform.runLater(() -> applyCenterCrop(ImageHolder, img));
              }
            });
    ImageHolder.setImage(img);
  }

  /** Name, description, price, sellers, image — shared by live-countdown and static-time rows. */
  private void patchItemCoreFields(Item item) {
    if (item == null || item.getId() != this.id) return;

    String name = item.getName() != null ? item.getName() : "";
    String desc = item.getDescription() != null ? item.getDescription() : "";
    String newSeller = item.getSellerUsername() != null ? item.getSellerUsername() : "";
    String newSellerAvatar = item.getSellerAvatarUrl() != null ? item.getSellerAvatarUrl() : "";
    String newImageUrl = item.getImageUrl() != null ? item.getImageUrl() : "";

    this.itemName = name;
    this.description = desc;
    this.sellerName = newSeller;
    this.sellerAvatarUrl = newSellerAvatar;
    if (ItemName != null) ItemName.setText(name);
    if (ItemDescription != null) ItemDescription.setText(desc);

    double price = item.getCurrentPrice();
    if (Double.compare(this.currentPrice, price) != 0) {
      updatePrice(price);
    }

    if (ImageHolder == null) return;
    if (newImageUrl.isBlank()) {
      if (this.imageUrl != null && !this.imageUrl.isBlank()) {
        ImageHolder.setImage(null);
      }
      this.imageUrl = newImageUrl;
    } else if (!Objects.equals(this.imageUrl, newImageUrl)) {
      this.imageUrl = newImageUrl;
      loadImageIfPresent(newImageUrl);
    }
  }

  /**
   * Applies server fields without reloading the image if the URL is unchanged (reduces lag on refresh).
   */
  public void syncFromCatalogItem(Item item) {
    if (item == null || item.getId() != this.id) return;
    catalogItemSnapshot = item;
    refreshPriceMetricCaption();
    patchItemCoreFields(item);
    setEndTime(item.getEndTime());
    updateTimeLabel();
  }

  /**
   * Same as {@link #syncFromCatalogItem} but time column is a fixed caption (History / Your items), no live
   * countdown.
   */
  public void syncFromCatalogItemStaticTime(Item item, String timeRemainCaption) {
    if (item == null || item.getId() != this.id) return;
    catalogItemSnapshot = item;
    refreshPriceMetricCaption();
    patchItemCoreFields(item);
    setEndTime(null);
    this.timeLabel = timeRemainCaption != null ? timeRemainCaption : "";
    if (TimeRemain != null) TimeRemain.setText(this.timeLabel);
  }

  private void applyCenterCrop(ImageView imageView, Image img) {
    double w = img.getWidth();
    double h = img.getHeight();
    double targetW = imageView.getFitWidth();
    double targetH = imageView.getFitHeight();

    double imgRatio = w / h;
    double targetRatio = targetW / targetH;

    double cropW, cropH, cropX, cropY;
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

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public void updateTimeLabel() {
    if (TimeRemain == null) return;
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime target;
    boolean dutchAuction =
        catalogItemSnapshot != null
            && catalogItemSnapshot.getAuctionType() == AuctionType.DUTCH
            && catalogItemSnapshot.getEndTime() != null;
    if (dutchAuction) {
      target = DutchAuctionPricing.countdownTarget(catalogItemSnapshot, now);
    } else {
      target = endTime;
    }
    if (target == null) {
      return;
    }
    Duration rem = Duration.between(now, target);
    String label;
    if (rem.isNegative() || rem.isZero()) {
      label = "closed";
    } else {
      long s = rem.getSeconds();
      long days = s / 86400;
      long hours = (s % 86400) / 3600;
      long mins = (s % 3600) / 60;
      long secs = s % 60;
      if (days > 0) label = days + "d " + hours + "h";
      else if (hours > 0) label = hours + "h " + mins + "m";
      else label = mins + "m " + secs + "s";
    }
    TimeRemain.setText(label);
  }

  private void updatePrice(double newPrice) {
    this.currentPrice = newPrice;
    if (Price != null) Price.setText(String.format("%,.0f$", newPrice));
  }

  public void handleItemClicked() {
    try {
      NodeContentLoader<ScrollPane> detailLoader = new NodeContentLoader<>();
      detailLoader.load("/fxml/iteminformation/ItemInformation.fxml");
      ItemInformationController detailController = detailLoader.getController();
      if (detailController != null) {
        detailController.setData(id, itemName, currentPrice, 0, description, timeLabel, imageUrl, sellerName, sellerAvatarUrl);
        detailController.refresh();
        KhungController.itemDetailController = detailController;
      }
      NodeManager.switchNodewithNode(
          detailLoader.getCurrentNode(),
          KhungController.getCurrentNode(),
          KhungController.getMainContentPane());
      KhungController.setMainContentNode(detailLoader.getCurrentNode());
    } catch (Exception ignored) {
    }
  }
}
