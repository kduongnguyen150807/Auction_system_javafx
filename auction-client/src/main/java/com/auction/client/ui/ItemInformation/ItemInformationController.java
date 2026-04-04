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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.geometry.Rectangle2D;

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
    private double currentmaxprice = 0;
    private int sellerid = -1;
    private int winnerid = -1;
    private java.util.List<Rating> cachedratings = new java.util.ArrayList<>();
    private javafx.scene.chart.XYChart.Series<String, Number> ans1 = new javafx.scene.chart.XYChart.Series<>();

    public void setData(int res, String ans, double res1, double ans2, String res2, String ans3, String res3, String ans4, String res4) {
        this.id = res;
        this.n = (ans == null) ? "" : ans;
        this.currentmaxprice = ans2;
        if (ItemName != null) ItemName.setText(this.n);
        if (ItemDescription != null) ItemDescription.setText(res2 == null ? "" : res2);
        if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", res1));
        if (MaxPriceValue != null) {
            if (ans2 > 0) MaxPriceValue.setText(String.format("BUY IT NOW: %,.0f$", ans2));
            else MaxPriceValue.setText("NO BUY IT NOW");
        }
        if (EndsInValue != null) EndsInValue.setText(ans3 == null ? "" : ans3);

        if (BidButton != null) {
            if (ans3 != null && (ans3.toLowerCase().startsWith("winner") || ans3.equalsIgnoreCase("closed"))) {
                BidButton.setText("CLOSED");
                BidButton.setDisable(true);
                BidButton.setStyle("-fx-background-color: #555555; -fx-text-fill: #999999; -fx-cursor: default;");
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
        if (SellerName != null) SellerName.setText(ans4 == null || ans4.isBlank() ? "Unknown Seller" : ans4);
        if (SellerAvatar != null && res4 != null && !res4.isBlank()) {
            String ans6 = res4.contains(".webp") ? res4.replace(".webp", ".jpg") : res4;
            Image res5 = new Image(ans6, true);
            res5.progressProperty().addListener((obs, oldv, newv) -> {
                if (newv.doubleValue() == 1.0) {
                    Platform.runLater(() -> {
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
        Thread ans = new Thread(() -> {
            try {
                Request res = new Request(Request.getitembyid, this.id);
                Response ans2 = NetworkClient.getinstance().sendrequestandwait(res);
                if (ans2 != null && Response.ok.equals(ans2.getstatus())) {
                    Object res1 = ans2.getpayload();
                    if (res1 instanceof Item i) {
                        Platform.runLater(() -> {
                            if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", i.getcurrentprice()));
                            this.currentmaxprice = i.getmaxprice();
                            if (MaxPriceValue != null) {
                                if (this.currentmaxprice > 0) MaxPriceValue.setText(String.format("BUY IT NOW: %,.0f$", this.currentmaxprice));
                                else MaxPriceValue.setText("NO BUY IT NOW");
                            }
                            this.sellerid = i.getsellerid();
                            this.winnerid = i.getwinnerid();
                            setupratingui(i);
                        });
                    }
                }
                loadratings();
                loadbidhistory();
            } catch (Exception e) {}
        });
        ans.setDaemon(true);
        ans.start();
    }

    private void loadbidhistory() {
        try {
            Request res = new Request("get_bid_history", this.id);
            Response ans = NetworkClient.getinstance().sendrequestandwait(res);
            if (ans != null && Response.ok.equals(ans.getstatus())) {
                java.util.List<BidTransaction> res1 = (java.util.List<BidTransaction>) ans.getpayload();
                Platform.runLater(() -> {
                    if (pricechart != null) {
                        pricechart.getData().clear();
                        ans1.getData().clear();
                        ans1.setName("Price Curve");
                        java.time.format.DateTimeFormatter ans2 = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
                        for (BidTransaction res2 : res1) {
                            String ans3 = res2.gettimestamp() != null ? res2.gettimestamp().format(ans2) : "";
                            ans1.getData().add(new javafx.scene.chart.XYChart.Data<>(ans3, res2.getbidvalue()));
                        }
                        pricechart.getData().add(ans1);
                    }
                });
            }
        } catch (Exception e) {}
    }

    private void setupratingui(Item res) {
        if (res == null || RateButton == null) return;
        boolean ans = false;
        if (res.getstatus() == com.auction.shared.ItemStatus.CLOSED || res.getstatus() == com.auction.shared.ItemStatus.FINISHED) {
            if (ClientSession.getCurrentUser() != null) {
                int res1 = ClientSession.getCurrentUser().getid();
                if (res1 == res.getwinnerid() || res1 == res.getsellerid()) ans = true;
            }
        }
        RateButton.setVisible(ans);
        RateButton.setManaged(ans);
    }

    private void loadratings() {
        if (this.id <= 0) return;
        Request res = new Request(Request.getratings, this.id);
        Response ans = NetworkClient.getinstance().sendrequestandwait(res);
        if (ans != null && Response.ok.equals(ans.getstatus())) {
            Object res1 = ans.getpayload();
            if (res1 instanceof List<?> list) {
                cachedratings.clear();
                for (Object o : list) {
                    if (o instanceof Rating) cachedratings.add((Rating) o);
                }
                Platform.runLater(() -> {
                    if (RatingFilterCombo != null && RatingFilterCombo.getItems().isEmpty()) {
                        RatingFilterCombo.getItems().addAll("All", "Positive", "Neutral", "Negative");
                        RatingFilterCombo.setValue("All");
                    }
                    renderratings("All");
                });
            }
        }
    }

    @FXML
    private void handleratingfilter() {
        if (RatingFilterCombo == null) return;
        String res = RatingFilterCombo.getValue();
        if (res == null) res = "All";
        renderratings(res);
    }

    private void renderratings(String res) {
        if (RatingsContainer == null) return;
        RatingsContainer.getChildren().removeIf(ans -> !(ans instanceof HBox));
        boolean ans1 = false;
        for (Rating res1 : cachedratings) {
            String ans2 = res1.getstars() <= 2 ? "Negative" : (res1.getstars() == 3 ? "Neutral" : "Positive");
            if (!"All".equals(res) && !ans2.equals(res)) continue;
            ans1 = true;
            String res2 = "\u2605".repeat(res1.getstars()) + "\u2606".repeat(5 - res1.getstars());
            String ans3 = res1.getstars() <= 2 ? "#ff4444" : (res1.getstars() == 3 ? "#ffaa00" : "#44ff44");
            String res3 = (res1.getraterusername() != null ? res1.getraterusername() : "User") + ": " + res2 + "  [" + ans2 + "]";
            Label ans4 = new Label(res3);
            ans4.setStyle("-fx-text-fill: " + ans3 + "; -fx-font-size: 13;");
            RatingsContainer.getChildren().add(ans4);
            if (res1.getfeedback() != null && !res1.getfeedback().isBlank()) {
                Label res4 = new Label("  \"" + res1.getfeedback() + "\"");
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
    private void showbiddingform() {
        try {
            NodeContentLoader<VBox> res = new NodeContentLoader<>();
            res.load("/fxml/biddingform/BiddingForm.fxml");
            com.auction.client.ui.BiddingForm.BiddingFormController ans = res.getController();
            if (ans != null) {
                if (id > 0) ans.setData(id, n, currentmaxprice);
                ans.setParentController(this);
            }
            NodeManager.addNodeToPane(res, KhungController.getKhungChua());
        } catch (Exception e) {}
    }

    @FXML
    private void showratingform() {
        try {
            NodeContentLoader<VBox> res = new NodeContentLoader<>();
            res.load("/fxml/ratingform/RatingForm.fxml");
            com.auction.client.ui.RatingForm.RatingFormController ans = res.getController();
            if (ans != null) {
                ans.setdata(this.id);
                ans.setoncomplete(() -> {
                    RateButton.setVisible(false);
                    RateButton.setManaged(false);
                    new Thread(() -> loadratings()).start();
                });
            }
            NodeManager.addNodeToPane(res, KhungController.getKhungChua());
        } catch (Exception e) {}
    }

    @FXML
    private void handleautobid() {
        try {
            double res = Double.parseDouble(autobidfield.getText());
            int ans = ClientSession.getCurrentUser().getid();
            BidTransaction res1 = new BidTransaction(this.id, ans, 0);
            res1.setmaxautobid(res);
            res1.setisautobid(true);
            Request ans1 = new Request(Request.bid, res1);
            Response res2 = NetworkClient.getinstance().sendrequestandwait(ans1);
            if (res2 != null && Response.ok.equals(res2.getstatus())) {
                autobidfield.clear();
            }
        } catch (Exception e) {}
    }

    public int getid() {
        int res = this.id;
        return res;
    }

    public void updatepriceui(Item res) {
        if (res == null || res.getid() != this.id) return;
        if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", res.getcurrentprice()));
        Platform.runLater(() -> {
            if (pricechart != null && ans1 != null) {
                String ans2 = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                ans1.getData().add(new javafx.scene.chart.XYChart.Data<>(ans2, res.getcurrentprice()));
            }
        });
    }

    public void updateCurrentBid(double val) {
        Platform.runLater(() -> {
            if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", val));
            if (pricechart != null && ans1 != null) {
                String ans = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                ans1.getData().add(new javafx.scene.chart.XYChart.Data<>(ans, val));
            }
        });
    }

    public void markAsSold() {
        Platform.runLater(() -> {
            if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText("ĐÃ CHỐT ĐỨT");
            if (MaxPriceValue != null) MaxPriceValue.setText("SELLED");
            if (BidButton != null) {
                BidButton.setText("CLOSED");
                BidButton.setDisable(true);
                BidButton.setStyle("-fx-background-color: #555555; -fx-text-fill: #999999; -fx-cursor: default;");
                if (autobidbutton != null) autobidbutton.setDisable(true);
                if (autobidfield != null) autobidfield.setDisable(true);
            }
            if (EndsInValue != null) EndsInValue.setText("Winner: " + com.auction.client.ClientSession.getUsername());
        });
    }
}