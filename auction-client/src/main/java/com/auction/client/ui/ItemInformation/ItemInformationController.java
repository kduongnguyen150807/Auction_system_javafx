package com.auction.client.ui.ItemInformation;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.service.BiddingClientService;
import com.auction.client.ui.Main.KhungController;
import com.auction.client.util.ImagePresentationUtil;
import com.auction.shared.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemInformationController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ItemInformationController.class);

  @FXML private ImageView ItemImageHolder;
  @FXML private Label ItemName;
  @FXML private Label ItemDescription;
  @FXML private Label CurrentHighestBidValue;
  @FXML private Label MaxPriceValue;
  @FXML private Label EndsInValue;
  @FXML private ImageView SellerAvatar;
  @FXML private Label SellerName;
  @FXML private Button BidButton;
  @FXML private Button RateButton;
  @FXML private VBox RatingsContainer;
  @FXML private javafx.scene.control.ComboBox<String> RatingFilterCombo;
  @FXML private TextField autobidfield;
  @FXML private Button autobidbutton;
  @FXML private LineChart<String, Number> pricechart;

  private int itemId = -1;
  private String itemName = "";
  private double buyItNowPrice = 0;
  private List<Rating> cachedRatings = new ArrayList<>();

  private BidHistoryChartBinder chartBinder;

  private final BiddingClientService biddingClientService = new BiddingClientService();

  private BidHistoryChartBinder chartBinder() {
    if (pricechart == null) return null;
    if (chartBinder == null) chartBinder = new BidHistoryChartBinder(pricechart);
    return chartBinder;
  }

  public void setData(int id, String name, double currentPrice, double maxPrice,
      String description, String endsIn, String imageUrl, String sellerName, String sellerAvatar) {
    this.itemId = id;
    this.itemName = name == null ? "" : name;
    this.buyItNowPrice = maxPrice;
    if (ItemName != null) ItemName.setText(this.itemName);
    if (ItemDescription != null) ItemDescription.setText(description == null ? "" : description);
    if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", currentPrice));
    if (MaxPriceValue != null) MaxPriceValue.setText(maxPrice > 0 ? String.format("BUY IT NOW: %,.0f$", maxPrice) : "NO BUY IT NOW");
    if (EndsInValue != null) EndsInValue.setText(endsIn == null ? "" : endsIn);
    if (BidButton != null) setBidButtonClosed(endsIn != null && (endsIn.toLowerCase().startsWith("winner") || endsIn.equalsIgnoreCase("closed")));
    if (ItemImageHolder != null && imageUrl != null && !imageUrl.isBlank())
      ItemImageHolder.setImage(new Image(ImagePresentationUtil.safeImageUrl(imageUrl), true));
    if (SellerName != null) SellerName.setText(sellerName == null || sellerName.isBlank() ? "Unknown Seller" : sellerName);
    if (SellerAvatar != null && sellerAvatar != null && !sellerAvatar.isBlank())
      ImagePresentationUtil.loadCircularAvatar(SellerAvatar, sellerAvatar, 20);
  }

  private void setBidButtonClosed(boolean closed) {
    BidButton.setText(closed ? "CLOSED" : "PLACE BID NOW");
    BidButton.setDisable(closed);
    BidButton.setStyle(closed
        ? "-fx-background-color: #555555; -fx-text-fill: #999999; -fx-cursor: default;" : "");
    if (autobidbutton != null) autobidbutton.setDisable(closed);
    if (autobidfield != null) autobidfield.setDisable(closed);
  }

  public void refresh() {
    Thread t =
        new Thread(
            () -> {
              try {
                Item item = biddingClientService.getItemById(this.itemId);
                if (item != null) {
                  Platform.runLater(
                      () -> {
                        if (CurrentHighestBidValue != null)
                          CurrentHighestBidValue.setText(String.format("%,.0f$", item.getCurrentPrice()));
                        this.buyItNowPrice = item.getMaxPrice();
                        if (MaxPriceValue != null)
                          MaxPriceValue.setText(
                              buyItNowPrice > 0 ? String.format("BUY IT NOW: %,.0f$", buyItNowPrice) : "NO BUY IT NOW");
                        setupRatingUi(item);
                      });
                }
                loadRatings();
                loadBidHistory();
              } catch (Exception e) {
                LOGGER.warn("Item refresh failed for id={}", this.itemId, e);
              }
            });
    t.setDaemon(true);
    t.start();
  }

  private void loadBidHistory() {
    try {
      List<BidTransaction> hist = biddingClientService.getBidHistory(this.itemId);
      DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
      Platform.runLater(
          () -> {
            BidHistoryChartBinder b = chartBinder();
            if (b != null) b.loadHistory(hist, fmt);
          });
    } catch (Exception e) {
      LOGGER.warn("Failed to load bid history for item id={}", this.itemId, e);
    }
  }

  private void setupRatingUi(Item item) {
    if (item == null || RateButton == null) return;
    boolean canRate =
        (item.getStatus() == ItemStatus.CLOSED || item.getStatus() == ItemStatus.FINISHED)
            && ClientSession.getCurrentUser() != null
            && (ClientSession.getCurrentUser().getId() == item.getWinnerId()
                || ClientSession.getCurrentUser().getId() == item.getSellerId());
    RateButton.setVisible(canRate);
    RateButton.setManaged(canRate);
  }

  private void loadRatings() {
    if (this.itemId <= 0) return;
    List<Rating> list = biddingClientService.getRatings(this.itemId);
    cachedRatings.clear();
    cachedRatings.addAll(list);
    Platform.runLater(
        () -> {
          if (RatingFilterCombo != null && RatingFilterCombo.getItems().isEmpty()) {
            RatingFilterCombo.getItems().addAll("All", "Positive", "Neutral", "Negative");
            RatingFilterCombo.setValue("All");
          }
          renderRatings("All");
        });
  }

  @FXML
  private void handleRatingFilter() {
    renderRatings(RatingFilterCombo != null && RatingFilterCombo.getValue() != null ? RatingFilterCombo.getValue() : "All");
  }

  private void renderRatings(String filter) {
    RatingListRenderer.render(RatingsContainer, cachedRatings, filter);
  }

  @FXML
  private void showBiddingForm() {
    loadOverlay(
        "/fxml/biddingform/BiddingForm.fxml",
        o -> {
          com.auction.client.ui.BiddingForm.BiddingFormController c =
              (com.auction.client.ui.BiddingForm.BiddingFormController) o;
          if (c != null) {
            if (itemId > 0) c.setData(itemId, itemName, buyItNowPrice);
            c.setParentController(this);
          }
        });
  }

  @FXML
  private void showRatingForm() {
    loadOverlay(
        "/fxml/ratingform/RatingForm.fxml",
        o -> {
          com.auction.client.ui.RatingForm.RatingFormController c =
              (com.auction.client.ui.RatingForm.RatingFormController) o;
          if (c != null) {
            c.setData(this.itemId);
            c.setOnComplete(
                () -> {
                  RateButton.setVisible(false);
                  RateButton.setManaged(false);
                  new Thread(this::loadRatings).start();
                });
          }
        });
  }

  private void loadOverlay(String fxml, java.util.function.Consumer<Object> setup) {
    try {
      NodeContentLoader<VBox> l = new NodeContentLoader<>();
      l.load(fxml);
      setup.accept(l.getController());
      NodeManager.addNodeToPane(l, KhungController.getMainContentPane());
    } catch (Exception e) {
      LOGGER.warn("Failed to load overlay: {}", fxml, e);
    }
  }

  @FXML
  private void handleAutoBid() {
    if (ClientSession.getCurrentUser() == null) return;
    String raw = (autobidfield != null ? autobidfield.getText() : "").replace("$", "").replace(",", "").trim();
    if (raw.isBlank()) {
      alert(Alert.AlertType.WARNING, "Invalid", "Enter your maximum auto-bid amount.");
      return;
    }
    try {
      double maxBid = Double.parseDouble(raw);
      if (maxBid <= 0) {
        alert(Alert.AlertType.WARNING, "Invalid", "Max auto-bid must be a positive number.");
        return;
      }
      BidTransaction bid = new BidTransaction(this.itemId, ClientSession.getCurrentUser().getId(), 0);
      bid.setMaxAutoBid(maxBid);
      bid.setAutoBid(true);
      Thread t =
          new Thread(
              () -> {
                Response res = biddingClientService.placeBid(bid);
                Platform.runLater(
                    () -> {
                      if (res != null && Response.OK.equals(res.getStatus())) {
                        if (autobidfield != null) autobidfield.clear();
                        alert(
                            Alert.AlertType.INFORMATION,
                            "Auto-Bid Active",
                            String.format("Auto-bid set! Will bid up to %,.0f$", maxBid));
                      } else
                        alert(
                            Alert.AlertType.ERROR,
                            "Auto-Bid Failed",
                            res != null ? res.getMessage() : "Failed to set auto-bid.");
                    });
              });
      t.setDaemon(true);
      t.start();
    } catch (NumberFormatException e) {
      alert(Alert.AlertType.ERROR, "Invalid", "Please enter a valid number.");
    }
  }

  private void alert(Alert.AlertType type, String title, String content) {
    try {
      Alert a = new Alert(type);
      a.setTitle(title);
      a.setHeaderText(null);
      a.setContentText(content);
      a.showAndWait();
    } catch (Exception ignored) {
    }
  }

  public void updatePriceUi(Item item) {
    if (item == null || item.getId() != this.itemId) return;
    if (Platform.isFxApplicationThread()) applyPriceFromItem(item);
    else Platform.runLater(() -> applyPriceFromItem(item));
  }

  private void applyPriceFromItem(Item item) {
    if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", item.getCurrentPrice()));
    if (EndsInValue != null && item.getEndTime() != null) {
      java.time.Duration rem = java.time.Duration.between(java.time.LocalDateTime.now(), item.getEndTime());
      if (!rem.isNegative() && !rem.isZero()) {
        long h = rem.toHours();
        EndsInValue.setText(
            h / 24 > 0
                ? (h / 24) + "d " + (h % 24) + "h"
                : (h % 24) + "h " + (rem.toMinutes() % 60) + "m " + (rem.getSeconds() % 60) + "s");
      }
    }
    appendPriceToChart(item.getCurrentPrice());
  }

  private void appendPriceToChart(double price) {
    BidHistoryChartBinder b = chartBinder();
    if (b != null) b.appendLivePrice(price, 20);
  }

  public void updateCurrentBid(double price) {
    Runnable update =
        () -> {
          if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", price));
          appendPriceToChart(price);
        };
    if (Platform.isFxApplicationThread()) update.run();
    else Platform.runLater(update);
  }

  public void markItemClosed(Item item) {
    if (item == null || item.getId() != this.itemId) return;
    Platform.runLater(
        () -> {
          setBidButtonClosed(true);
          if (EndsInValue != null) EndsInValue.setText("Auction Closed");
        });
  }

  public void markAsSold() {
    Platform.runLater(
        () -> {
          if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText("ĐÃ CHỐT ĐỨT");
          if (MaxPriceValue != null) MaxPriceValue.setText("SELLED");
          setBidButtonClosed(true);
          if (EndsInValue != null) EndsInValue.setText("Winner: " + ClientSession.getUsername());
        });
  }
}
