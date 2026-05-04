package com.auction.client.ui.ItemInformation;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

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
  @FXML private javafx.scene.chart.LineChart<String, Number> pricechart;

  private int itemId = -1;
  private String itemName = "";
  private double buyItNowPrice = 0;
  private int sellerId = -1;
  private int winnerId = -1;
  private java.util.List<Rating> cachedRatings = new java.util.ArrayList<>();
  private javafx.scene.chart.XYChart.Series<String, Number> priceSeries =
      new javafx.scene.chart.XYChart.Series<>();

  public void setData(int id, String name, double currentPrice, double maxPrice,
      String description, String endsIn, String imageUrl, String sellerName, String sellerAvatar) {
    this.itemId = id; this.itemName = name == null ? "" : name; this.buyItNowPrice = maxPrice;
    if (ItemName != null) ItemName.setText(this.itemName);
    if (ItemDescription != null) ItemDescription.setText(description == null ? "" : description);
    if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", currentPrice));
    if (MaxPriceValue != null) MaxPriceValue.setText(maxPrice > 0 ? String.format("BUY IT NOW: %,.0f$", maxPrice) : "NO BUY IT NOW");
    if (EndsInValue != null) EndsInValue.setText(endsIn == null ? "" : endsIn);
    if (BidButton != null) setBidButtonClosed(endsIn != null && (endsIn.toLowerCase().startsWith("winner") || endsIn.equalsIgnoreCase("closed")));
    if (ItemImageHolder != null && imageUrl != null && !imageUrl.isBlank()) ItemImageHolder.setImage(new Image(safeUrl(imageUrl), true));
    if (SellerName != null) SellerName.setText(sellerName == null || sellerName.isBlank() ? "Unknown Seller" : sellerName);
    if (SellerAvatar != null && sellerAvatar != null && !sellerAvatar.isBlank()) loadCircularAvatar(SellerAvatar, safeUrl(sellerAvatar), 20);
  }

  private String safeUrl(String url) {
    return url.contains(".webp") ? url.replace(".webp", ".jpg") : url;
  }

  private void loadCircularAvatar(ImageView view, String url, double radius) {
    Image img = new Image(url, true);
    img.progressProperty().addListener((obs, oldv, newv) -> {
      if (newv.doubleValue() == 1.0) Platform.runLater(() -> {
        double w = img.getWidth(), h = img.getHeight(), side = Math.min(w, h);
        view.setViewport(new Rectangle2D((w - side) / 2, (h - side) / 2, side, side));
        view.setImage(img);
        view.setClip(new Circle(radius, radius, radius));
      });
    });
    if (!img.isError()) view.setImage(img);
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
    Thread t = new Thread(() -> { try {
      Response res = NetworkClient.getInstance().sendRequestAndWait(new Request(Request.GET_ITEM_BY_ID, this.itemId));
      if (res != null && Response.OK.equals(res.getStatus()) && res.getPayload() instanceof Item item) {
        Platform.runLater(() -> {
          if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", item.getCurrentPrice()));
          this.buyItNowPrice = item.getMaxPrice();
          if (MaxPriceValue != null) MaxPriceValue.setText(buyItNowPrice > 0 ? String.format("BUY IT NOW: %,.0f$", buyItNowPrice) : "NO BUY IT NOW");
          this.sellerId = item.getSellerId(); this.winnerId = item.getWinnerId(); setupRatingUi(item);
        });
      }
      loadRatings(); loadBidHistory();
    } catch (Exception e) { LOGGER.warn("Item refresh failed for id={}", this.itemId, e); } });
    t.setDaemon(true); t.start();
  }

  private void loadBidHistory() {
    try {
      Response res = NetworkClient.getInstance().sendRequestAndWait(new Request("get_bid_history", this.itemId));
      if (res != null && Response.OK.equals(res.getStatus())) {
        @SuppressWarnings("unchecked") java.util.List<BidTransaction> hist = (java.util.List<BidTransaction>) res.getPayload();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        Platform.runLater(() -> { if (pricechart != null) {
          pricechart.getData().clear(); priceSeries.getData().clear(); priceSeries.setName("Price Curve");
          hist.forEach(b -> priceSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(b.getTimestamp() != null ? b.getTimestamp().format(fmt) : "", b.getBidValue())));
          pricechart.getData().add(priceSeries);
        } });
      }
    } catch (Exception e) { LOGGER.warn("Failed to load bid history for item id={}", this.itemId, e); }
  }

  private void setupRatingUi(Item item) {
    if (item == null || RateButton == null) return;
    boolean canRate = (item.getStatus() == ItemStatus.CLOSED || item.getStatus() == ItemStatus.FINISHED)
        && ClientSession.getCurrentUser() != null
        && (ClientSession.getCurrentUser().getId() == item.getWinnerId() || ClientSession.getCurrentUser().getId() == item.getSellerId());
    RateButton.setVisible(canRate); RateButton.setManaged(canRate);
  }

  private void loadRatings() {
    if (this.itemId <= 0) return;
    Response res = NetworkClient.getInstance().sendRequestAndWait(new Request(Request.GET_RATINGS, this.itemId));
    if (res != null && Response.OK.equals(res.getStatus()) && res.getPayload() instanceof List<?> list) {
      cachedRatings.clear(); list.forEach(e -> { if (e instanceof Rating r) cachedRatings.add(r); });
      Platform.runLater(() -> { if (RatingFilterCombo != null && RatingFilterCombo.getItems().isEmpty()) { RatingFilterCombo.getItems().addAll("All", "Positive", "Neutral", "Negative"); RatingFilterCombo.setValue("All"); } renderRatings("All"); });
    }
  }

  @FXML private void handleRatingFilter() {
    renderRatings(RatingFilterCombo != null && RatingFilterCombo.getValue() != null ? RatingFilterCombo.getValue() : "All");
  }

  private void renderRatings(String filter) {
    if (RatingsContainer == null) return;
    RatingsContainer.getChildren().removeIf(n -> !(n instanceof HBox));
    boolean any = false;
    for (Rating r : cachedRatings) {
      String s = r.getStars() <= 2 ? "Negative" : (r.getStars() == 3 ? "Neutral" : "Positive");
      if (!"All".equals(filter) && !s.equals(filter)) continue;
      any = true;
      String color = r.getStars() <= 2 ? "#ff4444" : (r.getStars() == 3 ? "#ffaa00" : "#44ff44");
      Label hdr = new Label((r.getRaterUsername() != null ? r.getRaterUsername() : "User") + ": "
          + "\u2605".repeat(r.getStars()) + "\u2606".repeat(5 - r.getStars()) + "  [" + s + "]");
      hdr.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13;");
      RatingsContainer.getChildren().add(hdr);
      if (r.getFeedback() != null && !r.getFeedback().isBlank()) {
        Label fb = new Label("  \"" + r.getFeedback() + "\"");
        fb.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12; -fx-font-style: italic;"); fb.setWrapText(true);
        RatingsContainer.getChildren().add(fb);
      }
    }
    if (!any && !"All".equals(filter)) { Label e = new Label("No " + filter.toLowerCase() + " ratings."); e.setStyle("-fx-text-fill: #666; -fx-font-size: 12;"); RatingsContainer.getChildren().add(e); }
    RatingsContainer.setVisible(!cachedRatings.isEmpty()); RatingsContainer.setManaged(!cachedRatings.isEmpty());
  }

  @FXML private void showBiddingForm() {
    loadOverlay("/fxml/biddingform/BiddingForm.fxml", o -> {
      com.auction.client.ui.BiddingForm.BiddingFormController c = (com.auction.client.ui.BiddingForm.BiddingFormController) o;
      if (c != null) { if (itemId > 0) c.setData(itemId, itemName, buyItNowPrice); c.setParentController(this); }
    });
  }
  @FXML private void showRatingForm() {
    loadOverlay("/fxml/ratingform/RatingForm.fxml", o -> {
      com.auction.client.ui.RatingForm.RatingFormController c = (com.auction.client.ui.RatingForm.RatingFormController) o;
      if (c != null) { c.setData(this.itemId); c.setOnComplete(() -> { RateButton.setVisible(false); RateButton.setManaged(false); new Thread(this::loadRatings).start(); }); }
    });
  }
  private void loadOverlay(String fxml, java.util.function.Consumer<Object> setup) {
    try { NodeContentLoader<VBox> l = new NodeContentLoader<>(); l.load(fxml); setup.accept(l.getController()); NodeManager.addNodeToPane(l, KhungController.getMainContentPane()); }
    catch (Exception e) { LOGGER.warn("Failed to load overlay: {}", fxml, e); }
  }

  @FXML private void handleAutoBid() {
    if (ClientSession.getCurrentUser() == null) return;
    String raw = (autobidfield != null ? autobidfield.getText() : "").replace("$", "").replace(",", "").trim();
    if (raw.isBlank()) { alert(javafx.scene.control.Alert.AlertType.WARNING, "Invalid", "Enter your maximum auto-bid amount."); return; }
    try {
      double maxBid = Double.parseDouble(raw);
      if (maxBid <= 0) { alert(javafx.scene.control.Alert.AlertType.WARNING, "Invalid", "Max auto-bid must be a positive number."); return; }
      BidTransaction bid = new BidTransaction(this.itemId, ClientSession.getCurrentUser().getId(), 0);
      bid.setMaxAutoBid(maxBid); bid.setAutoBid(true);
      Thread t = new Thread(() -> { Response res = NetworkClient.getInstance().sendRequestAndWait(new Request(Request.BID, bid));
        Platform.runLater(() -> { if (res != null && Response.OK.equals(res.getStatus())) { if (autobidfield != null) autobidfield.clear(); alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Auto-Bid Active", String.format("Auto-bid set! Will bid up to %,.0f$", maxBid)); }
          else alert(javafx.scene.control.Alert.AlertType.ERROR, "Auto-Bid Failed", res != null ? res.getMessage() : "Failed to set auto-bid."); }); });
      t.setDaemon(true); t.start();
    } catch (NumberFormatException e) { alert(javafx.scene.control.Alert.AlertType.ERROR, "Invalid", "Please enter a valid number."); }
  }

  private void alert(javafx.scene.control.Alert.AlertType type, String title, String content) {
    try { javafx.scene.control.Alert a = new javafx.scene.control.Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait(); }
    catch (Exception ignored) {}
  }

  public int getId() { return this.itemId; }

  public void updatePriceUi(Item item) {
    if (item == null || item.getId() != this.itemId) return;
    if (Platform.isFxApplicationThread()) applyPriceFromItem(item); else Platform.runLater(() -> applyPriceFromItem(item));
  }

  private void applyPriceFromItem(Item item) {
    if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", item.getCurrentPrice()));
    if (EndsInValue != null && item.getEndTime() != null) {
      java.time.Duration rem = java.time.Duration.between(java.time.LocalDateTime.now(), item.getEndTime());
      if (!rem.isNegative() && !rem.isZero()) {
        long h = rem.toHours();
        EndsInValue.setText(h / 24 > 0 ? (h / 24) + "d " + (h % 24) + "h" : (h % 24) + "h " + (rem.toMinutes() % 60) + "m " + (rem.getSeconds() % 60) + "s");
      }
    }
    appendPriceToChart(item.getCurrentPrice());
  }

  private void appendPriceToChart(double price) {
    if (pricechart == null || priceSeries == null) return;
    if (!pricechart.getData().contains(priceSeries)) { pricechart.getData().add(priceSeries); priceSeries.setName("Price Curve"); }
    priceSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(
        java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")), price));
    if (priceSeries.getData().size() > 20) priceSeries.getData().remove(0);
  }

  public void updateCurrentBid(double price) {
    Runnable update = () -> { if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", price)); appendPriceToChart(price); };
    if (Platform.isFxApplicationThread()) update.run(); else Platform.runLater(update);
  }

  public void markItemClosed(Item item) {
    if (item == null || item.getId() != this.itemId) return;
    Platform.runLater(() -> { setBidButtonClosed(true); if (EndsInValue != null) EndsInValue.setText("Auction Closed"); });
  }

  public void markAsSold() {
    Platform.runLater(() -> {
      if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText("ĐÃ CHỐT ĐỨT");
      if (MaxPriceValue != null) MaxPriceValue.setText("SELLED");
      setBidButtonClosed(true);
      if (EndsInValue != null) EndsInValue.setText("Winner: " + com.auction.client.ClientSession.getUsername());
    });
  }
}
