package com.auction.client.ui.ItemInformation;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.*;
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

  private int id = -1;
  private String n = "";
  private double currentMaxPrice = 0;
  private int sellerId = -1;
  private int winnerId = -1;
  private java.util.List<Rating> cachedratings = new java.util.ArrayList<>();
  private javafx.scene.chart.XYChart.Series<String, Number> ans1 =
      new javafx.scene.chart.XYChart.Series<>();

  public void setData(
      int res,
      String ans,
      double res1,
      double ans2,
      String res2,
      String ans3,
      String res3,
      String ans4,
      String res4) {
    this.id = res;
    this.n = (ans == null) ? "" : ans;
    this.currentMaxPrice = ans2;
    if (ItemName != null) ItemName.setText(this.n);
    if (ItemDescription != null) ItemDescription.setText(res2 == null ? "" : res2);
    if (CurrentHighestBidValue != null)
      CurrentHighestBidValue.setText(String.format("%,.0f$", res1));
    if (MaxPriceValue != null) {
      if (ans2 > 0) MaxPriceValue.setText(String.format("BUY IT NOW: %,.0f$", ans2));
      else MaxPriceValue.setText("NO BUY IT NOW");
    }
    if (EndsInValue != null) EndsInValue.setText(ans3 == null ? "" : ans3);

    if (BidButton != null) {
      if (ans3 != null
          && (ans3.toLowerCase().startsWith("winner") || ans3.equalsIgnoreCase("closed"))) {
        BidButton.setText("CLOSED");
        BidButton.setDisable(true);
        BidButton.setStyle(
            "-fx-background-color: #555555; -fx-text-fill: #999999; -fx-cursor: default;");
        if (autobidbutton != null) autobidbutton.setDisable(true);
        if (autobidfield != null) autobidfield.setDisable(true);
      } else {
        BidButton.setText("PLACE BID NOW");
        BidButton.setDisable(false);
        BidButton.setStyle("");
        if (autobidbutton != null) autobidbutton.setDisable(false);
        if (autobidfield != null) autobidfield.setDisable(false);
      }
    }

    if (ItemImageHolder != null && res3 != null && !res3.isBlank()) {
      String ans5 = res3.contains(".webp") ? res3.replace(".webp", ".jpg") : res3;
      ItemImageHolder.setImage(new Image(ans5, true));
    }
    if (SellerName != null)
      SellerName.setText(ans4 == null || ans4.isBlank() ? "Unknown Seller" : ans4);
    if (SellerAvatar != null && res4 != null && !res4.isBlank()) {
      String ans6 = res4.contains(".webp") ? res4.replace(".webp", ".jpg") : res4;
      Image res5 = new Image(ans6, true);
      res5.progressProperty()
          .addListener(
              (obs, oldv, newv) -> {
                if (newv.doubleValue() == 1.0) {
                  Platform.runLater(
                      () -> {
                        double ans7 = res5.getWidth();
                        double res6 = res5.getHeight();
                        double ans8 = Math.min(ans7, res6);
                        double res7 = (ans7 - ans8) / 2;
                        double ans9 = (res6 - ans8) / 2;
                        SellerAvatar.setViewport(new Rectangle2D(res7, ans9, ans8, ans8));
                        SellerAvatar.setImage(res5);
                        SellerAvatar.setClip(new Circle(20, 20, 20));
                      });
                }
              });
      if (res5.isError()) SellerAvatar.setImage(null);
      else SellerAvatar.setImage(res5);
    }
  }

  public void refresh() {
    Thread ans =
        new Thread(
            () -> {
              try {
                Request res = new Request(Request.GET_ITEM_BY_ID, this.id);
                Response ans2 = NetworkClient.getInstance().sendRequestAndWait(res);
                if (ans2 != null && Response.OK.equals(ans2.getStatus())) {
                  Object res1 = ans2.getPayload();
                  if (res1 instanceof Item i) {
                    Platform.runLater(
                        () -> {
                          if (CurrentHighestBidValue != null)
                            CurrentHighestBidValue.setText(
                                String.format("%,.0f$", i.getCurrentPrice()));
                          this.currentMaxPrice = i.getMaxPrice();
                          if (MaxPriceValue != null) {
                            if (this.currentMaxPrice > 0)
                              MaxPriceValue.setText(
                                  String.format("BUY IT NOW: %,.0f$", this.currentMaxPrice));
                            else MaxPriceValue.setText("NO BUY IT NOW");
                          }
                          this.sellerId = i.getSellerId();
                          this.winnerId = i.getWinnerId();
                          setupRatingUi(i);
                        });
                  }
                }
                loadRatings();
                loadBidHistory();
              } catch (Exception e) {
              }
            });
    ans.setDaemon(true);
    ans.start();
  }

  private void loadBidHistory() {
    try {
      Request res = new Request("get_bid_history", this.id);
      Response ans = NetworkClient.getInstance().sendRequestAndWait(res);
      if (ans != null && Response.OK.equals(ans.getStatus())) {
        java.util.List<BidTransaction> res1 = (java.util.List<BidTransaction>) ans.getPayload();
        Platform.runLater(
            () -> {
              if (pricechart != null) {
                pricechart.getData().clear();
                ans1.getData().clear();
                ans1.setName("Price Curve");
                java.time.format.DateTimeFormatter ans2 =
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
                for (BidTransaction res2 : res1) {
                  String ans3 = res2.getTimestamp() != null ? res2.getTimestamp().format(ans2) : "";
                  ans1.getData()
                      .add(new javafx.scene.chart.XYChart.Data<>(ans3, res2.getBidValue()));
                }
                pricechart.getData().add(ans1);
              }
            });
      }
    } catch (Exception e) {
    }
  }

  private void setupRatingUi(Item res) {
    if (res == null || RateButton == null) return;
    boolean ans = false;
    if (res.getStatus() == com.auction.shared.ItemStatus.CLOSED
        || res.getStatus() == com.auction.shared.ItemStatus.FINISHED) {
      if (ClientSession.getCurrentUser() != null) {
        int res1 = ClientSession.getCurrentUser().getId();
        if (res1 == res.getWinnerId() || res1 == res.getSellerId()) ans = true;
      }
    }
    RateButton.setVisible(ans);
    RateButton.setManaged(ans);
  }

  private void loadRatings() {
    if (this.id <= 0) return;
    Request res = new Request(Request.GET_RATINGS, this.id);
    Response ans = NetworkClient.getInstance().sendRequestAndWait(res);
    if (ans != null && Response.OK.equals(ans.getStatus())) {
      Object res1 = ans.getPayload();
      if (res1 instanceof List<?> list) {
        cachedratings.clear();
        for (Object o : list) {
          if (o instanceof Rating) cachedratings.add((Rating) o);
        }
        Platform.runLater(
            () -> {
              if (RatingFilterCombo != null && RatingFilterCombo.getItems().isEmpty()) {
                RatingFilterCombo.getItems().addAll("All", "Positive", "Neutral", "Negative");
                RatingFilterCombo.setValue("All");
              }
              renderRatings("All");
            });
      }
    }
  }

  @FXML
  private void handleRatingFilter() {
    if (RatingFilterCombo == null) return;
    String res = RatingFilterCombo.getValue();
    if (res == null) res = "All";
    renderRatings(res);
  }

  private void renderRatings(String res) {
    if (RatingsContainer == null) return;
    RatingsContainer.getChildren().removeIf(ans -> !(ans instanceof HBox));
    boolean ans1 = false;
    for (Rating res1 : cachedratings) {
      String ans2 =
          res1.getStars() <= 2 ? "Negative" : (res1.getStars() == 3 ? "Neutral" : "Positive");
      if (!"All".equals(res) && !ans2.equals(res)) continue;
      ans1 = true;
      String res2 = "\u2605".repeat(res1.getStars()) + "\u2606".repeat(5 - res1.getStars());
      String ans3 =
          res1.getStars() <= 2 ? "#ff4444" : (res1.getStars() == 3 ? "#ffaa00" : "#44ff44");
      String res3 =
          (res1.getRaterUsername() != null ? res1.getRaterUsername() : "User")
              + ": "
              + res2
              + "  ["
              + ans2
              + "]";
      Label ans4 = new Label(res3);
      ans4.setStyle("-fx-text-fill: " + ans3 + "; -fx-font-size: 13;");
      RatingsContainer.getChildren().add(ans4);
      if (res1.getFeedback() != null && !res1.getFeedback().isBlank()) {
        Label res4 = new Label("  \"" + res1.getFeedback() + "\"");
        res4.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12; -fx-font-style: italic;");
        res4.setWrapText(true);
        RatingsContainer.getChildren().add(res4);
      }
    }
    if (!ans1 && !"All".equals(res)) {
      Label ans5 = new Label("No " + res.toLowerCase() + " ratings.");
      ans5.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
      RatingsContainer.getChildren().add(ans5);
    }
    boolean res5 = !cachedratings.isEmpty();
    RatingsContainer.setVisible(res5);
    RatingsContainer.setManaged(res5);
  }

  @FXML
  private void showBiddingForm() {
    try {
      NodeContentLoader<VBox> res = new NodeContentLoader<>();
      res.load("/fxml/biddingform/BiddingForm.fxml");
      com.auction.client.ui.BiddingForm.BiddingFormController ans = res.getController();
      if (ans != null) {
        if (id > 0) ans.setData(id, n, currentMaxPrice);
        ans.setParentController(this);
      }
      NodeManager.addNodeToPane(res, KhungController.getMainContentPane());
    } catch (Exception e) {
    }
  }

  @FXML
  private void showRatingForm() {
    try {
      NodeContentLoader<VBox> res = new NodeContentLoader<>();
      res.load("/fxml/ratingform/RatingForm.fxml");
      com.auction.client.ui.RatingForm.RatingFormController ans = res.getController();
      if (ans != null) {
        ans.setData(this.id);
        ans.setOnComplete(
            () -> {
              RateButton.setVisible(false);
              RateButton.setManaged(false);
              new Thread(() -> loadRatings()).start();
            });
      }
      NodeManager.addNodeToPane(res, KhungController.getMainContentPane());
    } catch (Exception e) {
    }
  }

  @FXML
  private void handleAutoBid() {
    try {
      double res = Double.parseDouble(autobidfield.getText());
      int ans = ClientSession.getCurrentUser().getId();
      BidTransaction res1 = new BidTransaction(this.id, ans, 0);
      res1.setMaxAutoBid(res);
      res1.setAutoBid(true);
      Request ans1 = new Request(Request.BID, res1);
      Response res2 = NetworkClient.getInstance().sendRequestAndWait(ans1);
      if (res2 != null && Response.OK.equals(res2.getStatus())) {
        autobidfield.clear();
      }
    } catch (Exception e) {
    }
  }

  public int getId() {
    int res = this.id;
    return res;
  }

  public void updatePriceUi(Item res) {
    if (res == null || res.getId() != this.id) return;
    Platform.runLater(
        () -> {
          if (CurrentHighestBidValue != null)
            CurrentHighestBidValue.setText(String.format("%,.0f$", res.getCurrentPrice()));
          if (EndsInValue != null && res.getEndTime() != null) {
            java.time.Duration ans =
                java.time.Duration.between(java.time.LocalDateTime.now(), res.getEndTime());
            if (!ans.isNegative() && !ans.isZero()) {
              long res1 = ans.toHours();
              if (res1 / 24 > 0) EndsInValue.setText((res1 / 24) + "d " + (res1 % 24) + "h");
              else
                EndsInValue.setText(
                    (res1 % 24)
                        + "h "
                        + (ans.toMinutes() % 60)
                        + "m "
                        + (ans.getSeconds() % 60)
                        + "s");
            }
          }
          if (pricechart != null && ans1 != null) {
            if (!pricechart.getData().contains(ans1)) {
              pricechart.getData().add(ans1);
              ans1.setName("Price Curve");
            }
            String ans2 =
                java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            ans1.getData().add(new javafx.scene.chart.XYChart.Data<>(ans2, res.getCurrentPrice()));
            if (ans1.getData().size() > 20) {
              ans1.getData().remove(0);
            }
          }
        });
  }

  public void updateCurrentBid(double val) {
    Platform.runLater(
        () -> {
          if (CurrentHighestBidValue != null)
            CurrentHighestBidValue.setText(String.format("%,.0f$", val));
        });
  }

  public void markAsSold() {
    Platform.runLater(
        () -> {
          if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText("ĐÃ CHỐT ĐỨT");
          if (MaxPriceValue != null) MaxPriceValue.setText("SELLED");
          if (BidButton != null) {
            BidButton.setText("CLOSED");
            BidButton.setDisable(true);
            BidButton.setStyle(
                "-fx-background-color: #555555; -fx-text-fill: #999999; -fx-cursor: default;");
            if (autobidbutton != null) autobidbutton.setDisable(true);
            if (autobidfield != null) autobidfield.setDisable(true);
          }
          if (EndsInValue != null)
            EndsInValue.setText("Winner: " + com.auction.client.ClientSession.getUsername());
        });
  }
}
