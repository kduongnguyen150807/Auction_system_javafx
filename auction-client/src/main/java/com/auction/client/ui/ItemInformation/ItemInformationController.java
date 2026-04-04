package com.auction.client.ui.ItemInformation;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private int id = -1;
    private String n = "";
    private double currentMaxPrice = 0;

    public void setData(int iid, String iname, double p, double mp, String d, String t, String url, String sname, String surl) {
        this.id = iid;
        this.n = (iname == null) ? "" : iname;
        this.currentMaxPrice = mp;
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
            } else {
                BidButton.setText("PLACE BID NOW");
                BidButton.setDisable(false);
                BidButton.setStyle("");
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
            if (img.isError()) {
                SellerAvatar.setImage(null);
            } else {
                SellerAvatar.setImage(img);
            }
        }
    }

    public void refresh() {
        Thread t = new Thread(() -> {
            try {
                Request req = new Request(Request.list, null);
                Response res = NetworkClient.getinstance().sendrequestandwait(req);
                if (res != null && Response.ok.equals(res.getstatus())) {
                    Object p = res.getpayload();
                    if (p instanceof List<?> list) {
                        for (Object o : list) {
                            if (o instanceof Item i && i.getid() == this.id) {
                                Platform.runLater(() -> {
                                    if (CurrentHighestBidValue != null) {
                                        CurrentHighestBidValue.setText(String.format("%,.0f$", i.getcurrentprice()));
                                    }
                                    this.currentMaxPrice = i.getmaxprice();
                                    if (MaxPriceValue != null) {
                                        if (this.currentMaxPrice > 0) MaxPriceValue.setText(String.format("BUY IT NOW: %,.0f$", this.currentMaxPrice));
                                        else MaxPriceValue.setText("NO BUY IT NOW");
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {}
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void ShowBiddingForm() {
        try {
            NodeContentLoader<VBox> l = new NodeContentLoader<>();
            l.load("/fxml/biddingform/BiddingForm.fxml");
            com.auction.client.ui.BiddingForm.BiddingFormController c = l.getController();
            if (c != null) {
                if (id > 0) c.setData(id, n, currentMaxPrice);
                c.setParentController(this);
            }
            NodeManager.addNodeToPane(l, KhungController.getKhungChua());
        } catch (Exception e) {}
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
            }
            if (EndsInValue != null) EndsInValue.setText("Winner: " + com.auction.client.ClientSession.getUsername());
        });
    }
}