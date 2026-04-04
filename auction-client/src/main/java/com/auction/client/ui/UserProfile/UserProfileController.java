package com.auction.client.ui.UserProfile;

import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.List;

public class UserProfileController {
    @FXML private ImageView avatarImageView;
    @FXML private Label usernameLabel, fullNameLabel, emailLabel;
    @FXML private Label ratingStarsLabel, ratingCountLabel, reputationWarning;
    @FXML private Label itemsBoughtLabel, itemsSoldLabel, roleLabel;
    @FXML private FlowPane itemsContainer;

    private User targetUser;

    public void setUser(User user) {
        this.targetUser = user;
        populateData();
        loadSellerItems();
    }

    private void populateData() {
        if (targetUser == null) return;

        usernameLabel.setText(targetUser.getusername());
        String fn = targetUser.getfullname();
        fullNameLabel.setText(fn != null && !fn.isBlank() && !fn.equals(targetUser.getusername()) ? fn : "");
        emailLabel.setText(targetUser.getemail() != null ? targetUser.getemail() : "N/A");
        roleLabel.setText(targetUser.getrole() != null ? targetUser.getrole().name() : "BIDDER");
        itemsBoughtLabel.setText(String.valueOf(targetUser.getitemsbought()));
        itemsSoldLabel.setText(String.valueOf(targetUser.getitemssold()));

        if (ratingStarsLabel != null) {
            if (targetUser.gettotalratings() > 0) {
                int stars = (int) Math.round(targetUser.getavgrating());
                String starStr = "\u2605".repeat(Math.max(0, Math.min(stars, 5))) + "\u2606".repeat(Math.max(0, 5 - Math.min(stars, 5)));
                String rep = targetUser.getavgrating() <= 2.0 ? "Negative" : (targetUser.getavgrating() <= 3.0 ? "Neutral" : "Positive");
                String col = targetUser.getavgrating() <= 2.0 ? "-fx-text-fill: #ff4444;" : (targetUser.getavgrating() <= 3.0 ? "-fx-text-fill: #ffaa00;" : "-fx-text-fill: #44cc44;");
                ratingStarsLabel.setText(starStr);
                ratingStarsLabel.setStyle("-fx-font-size: 16; " + col);
                ratingCountLabel.setText(String.format("%.1f (%d ratings) - %s", targetUser.getavgrating(), targetUser.gettotalratings(), rep));
            } else {
                ratingStarsLabel.setText("");
                ratingCountLabel.setText("No ratings yet");
            }
        }
        if (reputationWarning != null) {
            boolean warn = targetUser.getavgrating() < 2.0 && targetUser.gettotalratings() >= 3;
            reputationWarning.setText(warn ? "WARNING: Low reputation user" : "");
            reputationWarning.setVisible(warn);
            reputationWarning.setManaged(warn);
        }

        loadAvatar();
    }

    private void loadAvatar() {
        String url = targetUser.getavatarurl();
        if (url != null && !url.isBlank() && avatarImageView != null) {
            Image img = new Image(url, true);
            img.progressProperty().addListener((obs, ov, nv) -> {
                if (nv.doubleValue() == 1.0 && !img.isError()) {
                    double w = img.getWidth(), h = img.getHeight();
                    double min = Math.min(w, h);
                    double x = (w - min) / 2, y = (h - min) / 2;
                    Platform.runLater(() -> {
                        avatarImageView.setImage(img);
                        avatarImageView.setViewport(new javafx.geometry.Rectangle2D(x, y, min, min));
                        avatarImageView.setFitWidth(68);
                        avatarImageView.setFitHeight(68);
                        avatarImageView.setPreserveRatio(false);
                        avatarImageView.setClip(new Circle(34, 34, 34));
                    });
                }
            });
        }
    }

    private void loadSellerItems() {
        if (targetUser == null || itemsContainer == null) return;
        new Thread(() -> {
            Request req = new Request("get_my_items", targetUser.getid());
            Response res = NetworkClient.getinstance().sendrequestandwait(req);
            if (res != null && Response.ok.equals(res.getstatus())) {
                List<Item> items = (List<Item>) res.getpayload();
                Platform.runLater(() -> {
                    itemsContainer.getChildren().clear();
                    if (items == null || items.isEmpty()) {
                        Label empty = new Label("No listings found");
                        empty.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
                        itemsContainer.getChildren().add(empty);
                        return;
                    }
                    for (Item item : items) {
                        itemsContainer.getChildren().add(buildItemCard(item));
                    }
                });
            }
        }).start();
    }

    private VBox buildItemCard(Item item) {
        VBox card = new VBox(6);
        card.getStyleClass().add("item-card-mini");

        Label nameLabel = new Label(item.getname());
        nameLabel.getStyleClass().add("item-card-mini-name");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("$%,.0f", item.getcurrentprice()));
        priceLabel.getStyleClass().add("item-card-mini-price");

        Label statusLabel = new Label(item.getstatus() != null ? item.getstatus().name() : "");
        statusLabel.getStyleClass().add("item-card-mini-status");
        if (item.getstatus() == ItemStatus.CLOSED || item.getstatus() == ItemStatus.FINISHED) {
            statusLabel.setStyle("-fx-text-fill: #999;");
        }

        card.getChildren().addAll(nameLabel, priceLabel, statusLabel);
        return card;
    }

    @FXML
    public void handleBack() {
        KhungController.returnToAuction();
    }
}
