package com.auction.client.ui.SearchBar;

import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.client.util.NotificationPopup;
import com.auction.shared.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ThanhTimKiemController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Button bellButton;
    @FXML private ToggleButton itemsToggle;
    @FXML private ToggleButton usersToggle;
    @FXML private ToggleGroup searchModeGroup;

    private NotificationPopup ans;
    private Timer debounceTimer;
    private boolean isUserMode = false;
    private Popup userResultsPopup;
    private VBox popupContent;

    @FXML
    public void initialize() {
        ans = new NotificationPopup();
        itemsToggle.setSelected(true);

        userResultsPopup = new Popup();
        userResultsPopup.setAutoHide(true);
        popupContent = new VBox();
        popupContent.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10; -fx-border-color: #333; -fx-border-radius: 10; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0.3, 0, 4);");
        popupContent.setPrefWidth(500);
        popupContent.setMaxHeight(400);
        userResultsPopup.getContent().add(popupContent);

        searchField.textProperty().addListener((obs, ov, nv) -> {
            if (isUserMode) {
                debounceUserSearch(nv);
            } else {
                hideUserResults();
                KhungController.applySearchFilter(
                        nv,
                        categoryFilter.getValue() != null ? categoryFilter.getValue() : "All"
                );
            }
        });

        if (categoryFilter != null) {
            categoryFilter.valueProperty().addListener((obs, ov, nv) -> {
                if (!isUserMode) {
                    KhungController.applySearchFilter(
                            searchField.getText(),
                            nv != null ? nv : "All"
                    );
                }
            });
        }
    }

    @FXML
    public void onSearchModeChanged() {
        isUserMode = usersToggle.isSelected();
        if (isUserMode) {
            searchField.setPromptText("Search users...");
            categoryFilter.setVisible(false);
            categoryFilter.setManaged(false);
            String text = searchField.getText();
            if (text != null && !text.trim().isEmpty()) {
                debounceUserSearch(text);
            }
        } else {
            searchField.setPromptText("Search items...");
            categoryFilter.setVisible(true);
            categoryFilter.setManaged(true);
            hideUserResults();
            KhungController.applySearchFilter(
                    searchField.getText(),
                    categoryFilter.getValue() != null ? categoryFilter.getValue() : "All"
            );
        }
    }

    private void debounceUserSearch(String keyword) {
        if (debounceTimer != null) debounceTimer.cancel();
        if (keyword == null || keyword.trim().isEmpty()) {
            hideUserResults();
            return;
        }
        debounceTimer = new Timer(true);
        debounceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                searchUsers(keyword.trim());
            }
        }, 300);
    }

    private void searchUsers(String keyword) {
        Request req = new Request(Request.searchusers, keyword);
        Response res = NetworkClient.getinstance().sendrequestandwait(req);
        if (res != null && Response.ok.equals(res.getstatus())) {
            List<User> users = (List<User>) res.getpayload();
            Platform.runLater(() -> showUserResults(users));
        }
    }

    private void showUserResults(List<User> users) {
        popupContent.getChildren().clear();

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(380);
        scroll.setPrefWidth(500);
        scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        VBox list = new VBox(0);
        list.setStyle("-fx-background-color: #1a1a2e;");

        if (users == null || users.isEmpty()) {
            Label empty = new Label("No users found");
            empty.setStyle("-fx-text-fill: #888; -fx-font-size: 14; -fx-padding: 16;");
            list.getChildren().add(empty);
        } else {
            for (User user : users) {
                list.getChildren().add(buildUserRow(user));
            }
        }

        scroll.setContent(list);
        popupContent.getChildren().add(scroll);

        Bounds bounds = searchField.localToScreen(searchField.getBoundsInLocal());
        if (bounds != null) {
            userResultsPopup.show(searchField.getScene().getWindow(),
                    bounds.getMinX(),
                    bounds.getMaxY() + 4);
        }
    }

    private HBox buildUserRow(User user) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #252540; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

        ImageView avatar = new ImageView();
        avatar.setFitWidth(36);
        avatar.setFitHeight(36);
        avatar.setPreserveRatio(false);
        avatar.setClip(new Circle(18, 18, 18));

        String url = user.getavatarurl();
        if (url != null && !url.isBlank()) {
            Image img = new Image(url, 36, 36, true, true, true);
            avatar.setImage(img);
        }

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(user.getusername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        String fn = user.getfullname();
        Label subLabel = new Label(fn != null && !fn.isBlank() && !fn.equals(user.getusername()) ? fn : (user.getemail() != null ? user.getemail() : ""));
        subLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        info.getChildren().addAll(nameLabel, subLabel);

        VBox ratingBox = new VBox(0);
        ratingBox.setAlignment(Pos.CENTER_RIGHT);
        if (user.gettotalratings() > 0) {
            int stars = (int) Math.round(user.getavgrating());
            stars = Math.max(0, Math.min(stars, 5));
            Label starLabel = new Label("\u2605".repeat(stars));
            String col = user.getavgrating() <= 2.0 ? "#ff4444" : (user.getavgrating() <= 3.0 ? "#ffaa00" : "#44cc44");
            starLabel.setStyle("-fx-text-fill: " + col + "; -fx-font-size: 13;");
            Label countLabel = new Label(String.format("%.1f (%d)", user.getavgrating(), user.gettotalratings()));
            countLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11;");
            ratingBox.getChildren().addAll(starLabel, countLabel);
        } else {
            Label noRating = new Label("No ratings");
            noRating.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
            ratingBox.getChildren().add(noRating);
        }

        row.getChildren().addAll(avatar, info, ratingBox);

        row.setOnMouseClicked(e -> {
            hideUserResults();
            searchField.clear();
            KhungController.showUserProfile(user);
        });

        return row;
    }

    private void hideUserResults() {
        if (userResultsPopup != null && userResultsPopup.isShowing()) {
            userResultsPopup.hide();
        }
    }

    @FXML
    public void toggleNotifications() {
        Window res = bellButton.getScene().getWindow();
        Point2D res1 = bellButton.localToScene(0.0, 0.0);
        double res2 = res.getX() + res.getScene().getX() + res1.getX();
        double res3 = res.getY() + res.getScene().getY() + res1.getY() + bellButton.getHeight() + 10;
        ans.show(res, res2, res3);
    }
}
