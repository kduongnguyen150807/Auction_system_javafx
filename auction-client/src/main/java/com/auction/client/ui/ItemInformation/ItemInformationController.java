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

    private int id = -1;
    private String n = "";
    private double currentmaxprice = 0;
    private int sellerid = -1;
    private int winnerid = -1;
    private java.util.List<Rating> cachedratings = new java.util.ArrayList<>();

    public void setData(int iid, String iname, double p, double mp, String d, String t, String url, String sname, String surl) {
        this.id = iid;
        this.n = (iname == null) ? "" : iname;
        this.currentmaxprice = mp;
        if (ItemName != null) ItemName.setText(this.n);
        if (ItemDescription != null) ItemDescription.setText(d == null ? "" : d);
        if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", p));
        if (MaxPriceValue != null) {
            if (mp > 0) MaxPriceValue.setText(String.format("BUY IT NOW: %,.0f$", mp));
            else MaxPriceValue.setText("NO BUY IT NOW");
        }
        if (EndsInValue != null) EndsInValue.setText(t == null ? "" : t);

        if (BidButton != null) {
            if (t != null && (t.toLowerCase().startsWith("winner") || t.equalsIgnoreCase("closed"))) {
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

        if (ItemImageHolder != null && url != null && !url.isBlank()) {
            String res = url.contains(".webp") ? url.replace(".webp", ".jpg") : url;
            ItemImageHolder.setImage(new Image(res, true));
        }
        if (SellerName != null) SellerName.setText(sname == null || sname.isBlank() ? "Unknown Seller" : sname);
        if (SellerAvatar != null && surl != null && !surl.isBlank()) {
            String res = surl.contains(".webp") ? surl.replace(".webp", ".jpg") : surl;
            Image img = new Image(res, true);
            img.progressProperty().addListener((obs, oldv, newv) -> {
                if (newv.doubleValue() == 1.0) {
                    Platform.runLater(() -> {
                        double w = img.getWidth();
                        double h = img.getHeight();
                        double side = Math.min(w, h);
                        double x = (w - side) / 2;
                        double y = (h - side) / 2;
                        SellerAvatar.setViewport(new Rectangle2D(x, y, side, side));
                        SellerAvatar.setImage(img);
                        SellerAvatar.setClip(new Circle(20, 20, 20));
                    });
                }
            });
            if (img.isError()) SellerAvatar.setImage(null);
            else SellerAvatar.setImage(img);
        }
    }

    public void refresh() {
        Thread t = new Thread(() -> {
            try {
                Request req = new Request(Request.getitembyid, this.id);
                Response res = NetworkClient.getinstance().sendrequestandwait(req);
                if (res != null && Response.ok.equals(res.getstatus())) {
                    Object p = res.getpayload();
                    if (p instanceof Item i) {
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
            } catch (Exception e) {}
        });
        t.setDaemon(true);
        t.start();
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
        Response res1 = NetworkClient.getinstance().sendrequestandwait(res);
        if (res1 != null && Response.ok.equals(res1.getstatus())) {
            Object res2 = res1.getpayload();
            if (res2 instanceof List<?> list) {
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

    private void renderratings(String filter) {
        if (RatingsContainer == null) return;
        RatingsContainer.getChildren().removeIf(res -> !(res instanceof HBox));
        boolean ans = false;
        for (Rating res4 : cachedratings) {
            String res9 = res4.getstars() <= 2 ? "Negative" : (res4.getstars() == 3 ? "Neutral" : "Positive");
            if (!"All".equals(filter) && !res9.equals(filter)) continue;
            ans = true;
            String res5 = "\u2605".repeat(res4.getstars()) + "\u2606".repeat(5 - res4.getstars());
            String res10 = res4.getstars() <= 2 ? "#ff4444" : (res4.getstars() == 3 ? "#ffaa00" : "#44ff44");
            String res6 = (res4.getraterusername() != null ? res4.getraterusername() : "User") + ": " + res5 + "  [" + res9 + "]";
            Label res7 = new Label(res6);
            res7.setStyle("-fx-text-fill: " + res10 + "; -fx-font-size: 13;");
            RatingsContainer.getChildren().add(res7);
            if (res4.getfeedback() != null && !res4.getfeedback().isBlank()) {
                Label res8 = new Label("  \"" + res4.getfeedback() + "\"");
                res8.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12; -fx-font-style: italic;");
                res8.setWrapText(true);
                RatingsContainer.getChildren().add(res8);
            }
        }
        if (!ans && !"All".equals(filter)) {
            Label res11 = new Label("No " + filter.toLowerCase() + " ratings.");
            res11.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
            RatingsContainer.getChildren().add(res11);
        }
        boolean res12 = !cachedratings.isEmpty();
        RatingsContainer.setVisible(res12);
        RatingsContainer.setManaged(res12);
    }

    @FXML
    private void showbiddingform() {
        try {
            NodeContentLoader<VBox> l = new NodeContentLoader<>();
            l.load("/fxml/biddingform/BiddingForm.fxml");
            com.auction.client.ui.BiddingForm.BiddingFormController c = l.getController();
            if (c != null) {
                if (id > 0) c.setData(id, n, currentmaxprice);
                c.setParentController(this);
            }
            NodeManager.addNodeToPane(l, KhungController.getKhungChua());
        } catch (Exception e) {}
    }

    @FXML
    private void showratingform() {
        try {
            NodeContentLoader<VBox> res = new NodeContentLoader<>();
            res.load("/fxml/ratingform/RatingForm.fxml");
            com.auction.client.ui.RatingForm.RatingFormController res1 = res.getController();
            if (res1 != null) {
                res1.setdata(this.id);
                res1.setoncomplete(() -> {
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
    }

    public void updateCurrentBid(double val) {
        Platform.runLater(() -> {
            if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.0f$", val));
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