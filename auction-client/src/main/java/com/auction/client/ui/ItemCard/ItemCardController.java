package com.auction.client.ui.ItemCard;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.AuctionType;
import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Request;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class ItemCardController {
  private static final double fullimgw = 270;
  private static final double fullimgh = 240;
  private static final double compactimgw = 180;
  private static final double compactimgh = 158;
  public static final double categoryslotwidth = 208;

  @FXML private VBox itemRoot;
  @FXML private Label ItemName, ItemDescription, Price, TimeRemain;
  @FXML private Label priceMetricCaption;
  @FXML private ImageView ImageHolder;
  @FXML private Rectangle imageClip;

  @FXML private HBox sellerActionsRow;
  @FXML private Button btnEditMyItem;
  @FXML private Button btnDeleteMyItem;

  // Nút thả tim Watchlist
  @FXML private Label heartIcon;

  private int id;
  private String itemname, description, timelabel, imageurl, sellername, selleravatarurl;
  private double currentprice;
  private LocalDateTime endtime;
  private Item catalogitemsnapshot;

  public void attachCatalogItem(Item catalogitem) {
    this.catalogitemsnapshot = catalogitem;
    refreshpricemetriccaption();
  }

  public Item getAttachedCatalogItem() {
    return catalogitemsnapshot;
  }

  public void setCompactRowLayout(boolean compact) {
    if (itemRoot != null) {
      itemRoot.getStyleClass().remove("item-card-compact");
      if (compact) {
        itemRoot.getStyleClass().add("item-card-compact");
        itemRoot.setMinWidth(categoryslotwidth);
        itemRoot.setPrefWidth(categoryslotwidth);
        itemRoot.setMaxWidth(categoryslotwidth);
      } else {
        itemRoot.setMinWidth(Region.USE_COMPUTED_SIZE);
        itemRoot.setPrefWidth(Region.USE_COMPUTED_SIZE);
        itemRoot.setMaxWidth(Double.MAX_VALUE);
      }
    }
    double w = compact ? compactimgw : fullimgw;
    double h = compact ? compactimgh : fullimgh;
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
      ItemCardViewportCrop.apply(ImageHolder, ImageHolder.getImage());
    }
  }

  public VBox getRootNode() {
    VBox ans = itemRoot;
    return ans;
  }

  public void setData(int itemid, String name, double price, String desc, String timelabel, String imageurl, String sellername, String selleravatarurl) {
    this.id = itemid;
    this.itemname = name;
    this.currentprice = price;
    this.description = desc;
    this.timelabel = timelabel;
    this.imageurl = imageurl;
    this.sellername = sellername;
    this.selleravatarurl = selleravatarurl;
    if (ItemName != null) {
      ItemName.setText(this.itemname);
    }
    if (ItemDescription != null) {
      ItemDescription.setText(this.description);
    }
    if (Price != null) {
      Price.setText(String.format("%,.0f$", this.currentprice));
    }
    if (TimeRemain != null) {
      TimeRemain.setText(this.timelabel);
    }
    loadimageifpresent(this.imageurl);
    refreshpricemetriccaption();

    // Kích hoạt UI thả tim
    setupWatchlistUI();
  }

  private void refreshpricemetriccaption() {
    if (priceMetricCaption == null) {
      return;
    }
    boolean dutch = catalogitemsnapshot != null && catalogitemsnapshot.getAuctionType() == AuctionType.DUTCH;
    priceMetricCaption.setText(dutch ? "CURRENT PRICE" : "CURRENT BID");
  }

  /** Optional actions for seller "My Items" view. */
  public void configureSellerItemActions(boolean showEdit, Runnable onEdit, boolean showCancel, Runnable onCancel) {
    if (sellerActionsRow == null) {
      return;
    }
    boolean any = showEdit || showCancel;
    sellerActionsRow.setVisible(any);
    sellerActionsRow.setManaged(any);
    if (btnEditMyItem != null) {
      btnEditMyItem.setVisible(showEdit);
      btnEditMyItem.setManaged(showEdit);
      btnEditMyItem.setOnAction(
              ev -> {
                ev.consume();
                if (onEdit != null) onEdit.run();
              });
    }
    if (btnDeleteMyItem != null) {
      btnDeleteMyItem.setVisible(showCancel);
      btnDeleteMyItem.setManaged(showCancel);
      btnDeleteMyItem.setOnAction(
              ev -> {
                ev.consume();
                if (onCancel != null) onCancel.run();
              });
    }
  }

  private boolean isTargetUnderSellerActions(Node target) {
    if (sellerActionsRow == null || !sellerActionsRow.isVisible()) {
      return false;
    }
    Node n = target;
    while (n != null) {
      if (n == sellerActionsRow) {
        return true;
      }
      n = n.getParent();
    }
    return false;
  }

  private void loadimageifpresent(String url) {
    if (ImageHolder == null || url == null || url.isBlank()) {
      return;
    }
    Image img = new Image(url, true);
    img.progressProperty().addListener((obs, oldv, newv) -> {
      if (newv.doubleValue() == 1.0) {
        Platform.runLater(() -> ItemCardViewportCrop.apply(ImageHolder, img));
      }
    });
    ImageHolder.setImage(img);
  }

  private void patchitemcorefields(Item item) {
    if (item == null || item.getId() != this.id) {
      return;
    }
    String name = item.getName() != null ? item.getName() : "";
    String desc = item.getDescription() != null ? item.getDescription() : "";
    String newseller = item.getSellerUsername() != null ? item.getSellerUsername() : "";
    String newselleravatar = item.getSellerAvatarUrl() != null ? item.getSellerAvatarUrl() : "";
    String newimageurl = item.getImageUrl() != null ? item.getImageUrl() : "";
    this.itemname = name;
    this.description = desc;
    this.sellername = newseller;
    this.selleravatarurl = newselleravatar;
    if (ItemName != null) {
      ItemName.setText(name);
    }
    if (ItemDescription != null) {
      ItemDescription.setText(desc);
    }
    double price = item.getCurrentPrice();
    if (Double.compare(this.currentprice, price) != 0) {
      updateprice(price);
    }
    if (ImageHolder == null) {
      return;
    }
    if (newimageurl.isBlank()) {
      if (this.imageurl != null && !this.imageurl.isBlank()) {
        ImageHolder.setImage(null);
      }
      this.imageurl = newimageurl;
    } else if (!Objects.equals(this.imageurl, newimageurl)) {
      this.imageurl = newimageurl;
      loadimageifpresent(newimageurl);
    }
  }

  public void syncFromCatalogItem(Item item) {
    if (item == null || item.getId() != this.id) {
      return;
    }
    catalogitemsnapshot = item;
    refreshpricemetriccaption();
    patchitemcorefields(item);
    setEndTime(item.getEndTime());
    updateTimeLabel();
  }

  public void syncFromCatalogItemStaticTime(Item item, String timeremaincaption) {
    if (item == null || item.getId() != this.id) {
      return;
    }
    catalogitemsnapshot = item;
    refreshpricemetriccaption();
    patchitemcorefields(item);
    setEndTime(null);
    this.timelabel = timeremaincaption != null ? timeremaincaption : "";
    if (TimeRemain != null) {
      TimeRemain.setText(this.timelabel);
    }
  }

  public void setEndTime(LocalDateTime endtime) {
    this.endtime = endtime;
  }

  public void updateTimeLabel() {
    if (TimeRemain == null) {
      return;
    }
    LocalDateTime now = LocalDateTime.now();
    refreshDutchLivePrice(now);
    LocalDateTime target;
    boolean dutchauction = catalogitemsnapshot != null && catalogitemsnapshot.getAuctionType() == AuctionType.DUTCH && catalogitemsnapshot.getEndTime() != null;
    if (dutchauction) {
      target = DutchAuctionPricing.countdownTarget(catalogitemsnapshot, now);
    } else {
      target = endtime;
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
      if (days > 0) {
        label = days + "d " + hours + "h";
      } else if (hours > 0) {
        label = hours + "h " + mins + "m";
      } else {
        label = mins + "m " + secs + "s";
      }
    }
    TimeRemain.setText(label);
  }

  /** Recompute listed Dutch price from schedule (no server round-trip). */
  private void refreshDutchLivePrice(LocalDateTime now) {
    if (catalogitemsnapshot == null
        || catalogitemsnapshot.getAuctionType() != AuctionType.DUTCH
        || catalogitemsnapshot.getStatus() != ItemStatus.OPEN) {
      return;
    }
    double effective = DutchAuctionPricing.computeEffectivePrice(catalogitemsnapshot, now);
    if (Double.compare(currentprice, effective) != 0) {
      updateprice(effective);
      catalogitemsnapshot.setCurrentPrice(effective);
    }
  }

  private void updateprice(double newprice) {
    this.currentprice = newprice;
    if (Price != null) {
      if (catalogitemsnapshot != null && catalogitemsnapshot.getAuctionType() == AuctionType.DUTCH) {
        Price.setText(DutchAuctionPricing.formatListedPrice(newprice));
      } else {
        Price.setText(String.format("%,.0f$", newprice));
      }
    }
  }

  public void handleItemClicked(MouseEvent e) {
    if (e != null && e.getTarget() instanceof Node node && isTargetUnderSellerActions(node)) {
      return;
    }
    // Ngăn click xuyên nếu bấm vào nút thả tim
    if (e != null && e.getTarget() == heartIcon) {
      return;
    }
    try {
      NodeContentLoader<ScrollPane> detailloader = new NodeContentLoader<>();
      detailloader.load("/fxml/iteminformation/ItemInformation.fxml");
      ItemInformationController detailcontroller = detailloader.getController();
      if (detailcontroller != null) {
        ItemStatus status = catalogitemsnapshot != null ? catalogitemsnapshot.getStatus() : ItemStatus.OPEN;
        LocalDateTime start =
                catalogitemsnapshot != null ? catalogitemsnapshot.getStartTime() : null;
        detailcontroller.setData(id, itemname, currentprice, 0, description, timelabel, imageurl,
                sellername, selleravatarurl, status, start);
        detailcontroller.refresh();
        KhungController.itemDetailController = detailcontroller;
      }
      NodeManager.switchNodewithNode(detailloader.getCurrentNode(), KhungController.getCurrentNode(), KhungController.getMainContentPane());
      KhungController.setMainContentNode(detailloader.getCurrentNode());
    } catch (Exception ex) {
    }
  }

  // --- LOGIC WATCHLIST ---
  private void setupWatchlistUI() {
    if (heartIcon == null || ClientSession.getCurrentUser() == null) return;

    boolean isWatched = ClientSession.isWatching(this.id);
    setHeartUI(isWatched);

    heartIcon.setOnMouseClicked(e -> {
      e.consume();
      boolean newState = !ClientSession.isWatching(this.id);

      // 1. Cập nhật RAM
      ClientSession.toggleWatch(this.id, newState);

      // 2. Báo cho KhungController đồng bộ TẤT CẢ các thẻ trên màn hình
      KhungController.notifyWatchlistToggle(this.id, newState);

      // 3. Bắn request ngầm xuống Server
      new Thread(() -> {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("itemId", this.id);
        payload.put("isWatching", newState);
        com.auction.client.network.NetworkClient.getInstance()
                .sendRequestAndWait(new Request(Request.TOGGLE_WATCHLIST, payload));
      }).start();
    });
  }

  // Đổi thành public để các Controller khác có thể gọi vào
  public void setHeartUI(boolean isWatched) {
    if (heartIcon == null) return;
    Platform.runLater(() -> {
      heartIcon.setText(isWatched ? "❤" : "♡");
      heartIcon.setStyle(isWatched ? "-fx-text-fill: #ff2a6d; -fx-font-size: 24px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 2);"
              : "-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 2);");
    });
  }
}