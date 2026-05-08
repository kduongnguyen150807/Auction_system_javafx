package com.auction.client.ui.ItemInformation;

import com.auction.client.ClientSession;
import com.auction.client.service.BiddingClientService;
import com.auction.client.util.ImagePresentationUtil;
import com.auction.shared.AuctionType;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Rating;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemInformationController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ItemInformationController.class);

  @FXML private ImageView ItemImageHolder;
  @FXML private Label ItemName;
  @FXML private Label ItemDescription;
  @FXML private Label CurrentHighestBidValue;
  @FXML private Label BidMetricCaption;
  @FXML private Label MaxPriceValue;
  @FXML private Label EndsInValue;
  @FXML private ImageView SellerAvatar;
  @FXML private Label SellerName;
  @FXML private Button BidButton;
  @FXML private Button RateButton;
  @FXML private VBox RatingsContainer;
  @FXML private VBox buyItNowVBox;
  @FXML private HBox autoBidHBox;
  @FXML private javafx.scene.control.ComboBox<String> RatingFilterCombo;
  @FXML private TextField autobidfield;
  @FXML private Button autobidbutton;
  @FXML private LineChart<String, Number> pricechart;

  private int itemId = -1;
  private String itemName = "";
  private double buyItNowPrice = 0;
  private List<Rating> cachedRatings = new ArrayList<>();
  private AuctionType listingKind = AuctionType.ENGLISH;
  private double lastListedPrice = 0;
  /** Server snapshot for live “ends in” refresh (English end time + Dutch price windows). */
  private Item endsInSourceItem;

  private BidHistoryChartBinder chartBinder;
  private Timeline endsInTicker;

  private final BiddingClientService biddingClientService = new BiddingClientService();

  @FXML
  void initialize() {
    endsInTicker =
        new Timeline(new KeyFrame(Duration.seconds(1), e -> tickEndsInDisplay()));
    endsInTicker.setCycleCount(Timeline.INDEFINITE);
    endsInTicker.play();
  }

  private void tickEndsInDisplay() {
    if (EndsInValue == null || endsInSourceItem == null || itemId <= 0) {
      return;
    }
    if (BidButton != null && BidButton.isDisabled()) {
      return;
    }
    String t = EndsInValue.getText();
    if (t != null && t.startsWith("Winner:")) {
      return;
    }
    ItemInformationUiHelper.applyEndsIn(EndsInValue, endsInSourceItem);
  }

  private void setEndsInSource(Item item) {
    this.endsInSourceItem = item;
  }

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
    this.lastListedPrice = currentPrice;
    this.listingKind = AuctionType.ENGLISH;
    this.endsInSourceItem = null;
    if (ItemName != null) ItemName.setText(this.itemName);
    if (ItemDescription != null) ItemDescription.setText(description == null ? "" : description);
    if (CurrentHighestBidValue != null) {
      CurrentHighestBidValue.setText(ItemInformationUiHelper.formatPriceDollars(currentPrice));
    }
    if (MaxPriceValue != null) {
      MaxPriceValue.setText(ItemInformationUiHelper.formatBuyItNowLine(maxPrice));
    }
    if (EndsInValue != null) EndsInValue.setText(endsIn == null ? "" : endsIn);
    if (BidButton != null) {
      ItemInformationUiHelper.setBidButtonClosed(
          listingKind,
          BidButton,
          autobidfield,
          autobidbutton,
          endsIn != null
              && (endsIn.toLowerCase().startsWith("winner")
                  || endsIn.equalsIgnoreCase("closed")));
    }
    if (ItemImageHolder != null && imageUrl != null && !imageUrl.isBlank()) {
      ItemImageHolder.setImage(new Image(ImagePresentationUtil.safeImageUrl(imageUrl), true));
    }
    if (SellerName != null) {
      SellerName.setText(sellerName == null || sellerName.isBlank() ? "Unknown Seller" : sellerName);
    }
    if (SellerAvatar != null && sellerAvatar != null && !sellerAvatar.isBlank()) {
      ImagePresentationUtil.loadCircularAvatar(SellerAvatar, sellerAvatar, 20);
    }
    ItemInformationUiHelper.applyAuctionPresentation(
        listingKind, BidMetricCaption, buyItNowVBox, autoBidHBox, BidButton);
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
                        setEndsInSource(item);
                        listingKind = item.getAuctionType();
                        lastListedPrice = item.getCurrentPrice();
                        if (CurrentHighestBidValue != null) {
                          CurrentHighestBidValue.setText(
                              ItemInformationUiHelper.formatPriceDollars(item.getCurrentPrice()));
                        }
                        buyItNowPrice = item.getMaxPrice();
                        if (MaxPriceValue != null) {
                          MaxPriceValue.setText(
                              ItemInformationUiHelper.formatBuyItNowLine(buyItNowPrice));
                        }
                        ItemInformationUiHelper.applyAuctionPresentation(
                            listingKind,
                            BidMetricCaption,
                            buyItNowVBox,
                            autoBidHBox,
                            BidButton);
                        ItemInformationUiHelper.applyEndsIn(EndsInValue, item);
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
    renderRatings(
        RatingFilterCombo != null && RatingFilterCombo.getValue() != null
            ? RatingFilterCombo.getValue()
            : "All");
  }

  private void renderRatings(String filter) {
    RatingListRenderer.render(RatingsContainer, cachedRatings, filter);
  }

  @FXML
  private void showBiddingForm() {
    ItemInformationOverlayLoader.addToMainContent(
        "/fxml/biddingform/BiddingForm.fxml",
        o -> {
          com.auction.client.ui.BiddingForm.BiddingFormController c =
              (com.auction.client.ui.BiddingForm.BiddingFormController) o;
          if (c != null) {
            c.setData(
                itemId,
                itemName,
                buyItNowPrice,
                listingKind == AuctionType.DUTCH,
                lastListedPrice);
            c.setParentController(this);
          }
        });
  }

  @FXML
  private void showRatingForm() {
    ItemInformationOverlayLoader.addToMainContent(
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

  @FXML
  private void handleAutoBid() {
    ItemInformationAutoBidCoordinator.submitIfValid(
        this.itemId, listingKind, autobidfield, biddingClientService);
  }

  public void updatePriceUi(Item item) {
    if (item == null || item.getId() != this.itemId) return;
    if (Platform.isFxApplicationThread()) applyPriceFromItem(item);
    else Platform.runLater(() -> applyPriceFromItem(item));
  }

  private void applyPriceFromItem(Item item) {
    if (item == null || item.getId() != this.itemId) return;
    setEndsInSource(item);
    listingKind = item.getAuctionType();
    lastListedPrice = item.getCurrentPrice();
    if (CurrentHighestBidValue != null) {
      CurrentHighestBidValue.setText(
          ItemInformationUiHelper.formatPriceDollars(item.getCurrentPrice()));
    }
    buyItNowPrice = item.getMaxPrice();
    if (MaxPriceValue != null) {
      MaxPriceValue.setText(ItemInformationUiHelper.formatBuyItNowLine(buyItNowPrice));
    }
    ItemInformationUiHelper.applyAuctionPresentation(
        listingKind, BidMetricCaption, buyItNowVBox, autoBidHBox, BidButton);
    ItemInformationUiHelper.applyEndsIn(EndsInValue, item);
    appendPriceToChart(item.getCurrentPrice());
  }

  private void appendPriceToChart(double price) {
    BidHistoryChartBinder b = chartBinder();
    if (b != null) b.appendLivePrice(price, 20);
  }

  public void markItemClosed(Item item) {
    if (item == null || item.getId() != this.itemId) return;
    Platform.runLater(
        () -> {
          setEndsInSource(null);
          ItemInformationUiHelper.setBidButtonClosed(
              listingKind, BidButton, autobidfield, autobidbutton, true);
          if (EndsInValue != null) EndsInValue.setText("Auction Closed");
        });
  }

  public void markAsSold() {
    Platform.runLater(
        () -> {
          endsInSourceItem = null;
          if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText("ĐÃ CHỐT ĐỨT");
          if (MaxPriceValue != null) MaxPriceValue.setText("SELLED");
          ItemInformationUiHelper.setBidButtonClosed(
              listingKind, BidButton, autobidfield, autobidbutton, true);
          if (EndsInValue != null) EndsInValue.setText("Winner: " + ClientSession.getUsername());
        });
  }
}
