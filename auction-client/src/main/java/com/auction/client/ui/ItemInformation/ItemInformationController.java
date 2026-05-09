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

public class ItemInformationController {
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

    private int itemid = -1;
    private String itemname = "";
    private double buyitnowprice = 0;
    private List<Rating> cachedratings = new ArrayList<>();
    private AuctionType listingkind = AuctionType.ENGLISH;
    private double lastlistedprice = 0;
    private Item endsinsourceitem;
    private BidHistoryChartBinder chartbinder;
    private Timeline endsinticker;
    private final BiddingClientService biddingclientservice = new BiddingClientService();

    @FXML
    void initialize() {
        endsinticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> tickendsindisplay()));
        endsinticker.setCycleCount(Timeline.INDEFINITE);
        endsinticker.play();
    }

    private void tickendsindisplay() {
        if (EndsInValue == null || endsinsourceitem == null || itemid <= 0) {
            return;
        }
        if (BidButton != null && BidButton.isDisabled()) {
            return;
        }
        String t = EndsInValue.getText();
        if (t != null && t.startsWith("Winner:")) {
            return;
        }
        ItemInformationUiHelper.applyEndsIn(EndsInValue, endsinsourceitem);
    }

    private void setendsinsource(Item item) {
        this.endsinsourceitem = item;
    }

    private BidHistoryChartBinder chartbinder() {
        if (pricechart == null) {
            return null;
        }
        if (chartbinder == null) {
            chartbinder = new BidHistoryChartBinder(pricechart);
        }
        BidHistoryChartBinder ans = chartbinder;
        return ans;
    }

    public void setData(int id, String name, double currentprice, double maxprice, String description, String endsin, String imageurl, String sellername, String selleravatar, ItemStatus status) {
        this.itemid = id;
        this.itemname = name == null ? "" : name;
        this.buyitnowprice = maxprice;
        this.lastlistedprice = currentprice;
        this.listingkind = AuctionType.ENGLISH;
        this.endsinsourceitem = null;
        if (ItemName != null) {
            ItemName.setText(this.itemname);
        }
        if (ItemDescription != null) {
            ItemDescription.setText(description == null ? "" : description);
        }
        if (CurrentHighestBidValue != null) {
            CurrentHighestBidValue.setText(ItemInformationUiHelper.formatPriceDollars(currentprice));
        }
        if (MaxPriceValue != null) {
            MaxPriceValue.setText(ItemInformationUiHelper.formatBuyItNowLine(maxprice));
        }
        if (EndsInValue != null) {
            EndsInValue.setText(endsin == null ? "" : endsin);
        }
        if (BidButton != null) {
            boolean closed = status != ItemStatus.OPEN;
            ItemInformationUiHelper.setBidButtonClosed(listingkind, BidButton, autobidfield, autobidbutton, closed);
        }
        if (ItemImageHolder != null && imageurl != null && !imageurl.isBlank()) {
            ItemImageHolder.setImage(new Image(ImagePresentationUtil.safeImageUrl(imageurl), true));
        }
        if (SellerName != null) {
            SellerName.setText(sellername == null || sellername.isBlank() ? "Unknown Seller" : sellername);
        }
        if (SellerAvatar != null && selleravatar != null && !selleravatar.isBlank()) {
            ImagePresentationUtil.loadCircularAvatar(SellerAvatar, selleravatar, 20);
        }
        ItemInformationUiHelper.applyAuctionPresentation(listingkind, BidMetricCaption, buyItNowVBox, autoBidHBox, BidButton);
    }

    public void refresh() {
        Thread thread = new Thread(() -> {
            try {
                Item item = biddingclientservice.getItemById(this.itemid);
                if (item != null) {
                    Platform.runLater(() -> {
                        setendsinsource(item);
                        listingkind = item.getAuctionType();
                        lastlistedprice = item.getCurrentPrice();
                        if (CurrentHighestBidValue != null) {
                            CurrentHighestBidValue.setText(ItemInformationUiHelper.formatPriceDollars(item.getCurrentPrice()));
                        }
                        buyitnowprice = item.getMaxPrice();
                        if (MaxPriceValue != null) {
                            MaxPriceValue.setText(ItemInformationUiHelper.formatBuyItNowLine(buyitnowprice));
                        }
                        ItemInformationUiHelper.applyAuctionPresentation(listingkind, BidMetricCaption, buyItNowVBox, autoBidHBox, BidButton);
                        ItemInformationUiHelper.applyEndsIn(EndsInValue, item);
                        boolean closed = item.getStatus() != ItemStatus.OPEN;
                        ItemInformationUiHelper.setBidButtonClosed(listingkind, BidButton, autobidfield, autobidbutton, closed);
                        setupratingui(item);
                    });
                    loadbidhistory(item);
                }
                loadratings();
            } catch (Exception e) {
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void loadbidhistory(Item item) {
        try {
            List<BidTransaction> hist = biddingclientservice.getBidHistory(this.itemid);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            Platform.runLater(() -> {
                BidHistoryChartBinder b = chartbinder();
                if (b != null) {
                    b.loadHistory(item, hist, fmt);
                }
            });
        } catch (Exception e) {
        }
    }

    private void setupratingui(Item item) {
        if (item == null || RateButton == null) {
            return;
        }
        boolean canrate = (item.getStatus() == ItemStatus.CLOSED || item.getStatus() == ItemStatus.FINISHED)
                && ClientSession.getCurrentUser() != null
                && ClientSession.getCurrentUser().getId() == item.getWinnerId();
        RateButton.setVisible(canrate);
        RateButton.setManaged(canrate);
    }

    private void loadratings() {
        if (this.itemid <= 0) {
            return;
        }
        List<Rating> list = biddingclientservice.getRatings(this.itemid);
        cachedratings.clear();
        cachedratings.addAll(list);
        Platform.runLater(() -> {
            if (RatingFilterCombo != null && RatingFilterCombo.getItems().isEmpty()) {
                RatingFilterCombo.getItems().addAll("All", "Positive", "Neutral", "Negative");
                RatingFilterCombo.setValue("All");
            }
            renderratings("All");
        });
    }

    @FXML
    private void handleRatingFilter() {
        String filter = RatingFilterCombo != null && RatingFilterCombo.getValue() != null ? RatingFilterCombo.getValue() : "All";
        renderratings(filter);
    }

    private void renderratings(String filter) {
        RatingListRenderer.render(RatingsContainer, cachedratings, filter);
    }

    @FXML
    private void showBiddingForm() {
        ItemInformationOverlayLoader.addToMainContent("/fxml/biddingform/BiddingForm.fxml", o -> {
            com.auction.client.ui.BiddingForm.BiddingFormController c = (com.auction.client.ui.BiddingForm.BiddingFormController) o;
            if (c != null) {
                c.setData(itemid, itemname, buyitnowprice, listingkind == AuctionType.DUTCH, lastlistedprice);
                c.setParentController(this);
            }
        });
    }

    @FXML
    private void showRatingForm() {
        ItemInformationOverlayLoader.addToMainContent("/fxml/ratingform/RatingForm.fxml", o -> {
            com.auction.client.ui.RatingForm.RatingFormController c = (com.auction.client.ui.RatingForm.RatingFormController) o;
            if (c != null) {
                c.setData(this.itemid);
                c.setOnComplete(() -> {
                    RateButton.setVisible(false);
                    RateButton.setManaged(false);
                    new Thread(this::loadratings).start();
                });
            }
        });
    }

    @FXML
    private void handleAutoBid() {
        ItemInformationAutoBidCoordinator.submitIfValid(this.itemid, listingkind, autobidfield, biddingclientservice);
    }

    public void updatePriceUi(Item item) {
        if (item == null || item.getId() != this.itemid) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            applypricefromitem(item);
        } else {
            Platform.runLater(() -> applypricefromitem(item));
        }
    }

    private void applypricefromitem(Item item) {
        if (item == null || item.getId() != this.itemid) {
            return;
        }
        setendsinsource(item);
        listingkind = item.getAuctionType();
        lastlistedprice = item.getCurrentPrice();
        if (CurrentHighestBidValue != null) {
            CurrentHighestBidValue.setText(ItemInformationUiHelper.formatPriceDollars(item.getCurrentPrice()));
        }
        buyitnowprice = item.getMaxPrice();
        if (MaxPriceValue != null) {
            MaxPriceValue.setText(ItemInformationUiHelper.formatBuyItNowLine(buyitnowprice));
        }
        ItemInformationUiHelper.applyAuctionPresentation(listingkind, BidMetricCaption, buyItNowVBox, autoBidHBox, BidButton);
        ItemInformationUiHelper.applyEndsIn(EndsInValue, item);
        boolean closed = item.getStatus() != ItemStatus.OPEN;
        ItemInformationUiHelper.setBidButtonClosed(listingkind, BidButton, autobidfield, autobidbutton, closed);
        appendpricetochart(item.getCurrentPrice());
    }

    private void appendpricetochart(double price) {
        BidHistoryChartBinder b = chartbinder();
        if (b != null) {
            b.appendLivePrice(price, 20);
        }
    }

    public void markItemClosed(Item item) {
        if (item == null || item.getId() != this.itemid) {
            return;
        }
        Platform.runLater(() -> {
            setendsinsource(null);
            ItemInformationUiHelper.setBidButtonClosed(listingkind, BidButton, autobidfield, autobidbutton, true);
            if (EndsInValue != null) {
                EndsInValue.setText("Auction Closed");
            }
        });
    }

    public void markAsSold() {
        Platform.runLater(() -> {
            endsinsourceitem = null;
            if (CurrentHighestBidValue != null) {
                CurrentHighestBidValue.setText("ĐÃ CHỐT ĐỨT");
            }
            if (MaxPriceValue != null) {
                MaxPriceValue.setText("SELLED");
            }
            ItemInformationUiHelper.setBidButtonClosed(listingkind, BidButton, autobidfield, autobidbutton, true);
            if (EndsInValue != null) {
                EndsInValue.setText("Winner: " + ClientSession.getUsername());
            }
        });
    }
}