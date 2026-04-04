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
    private Timer res;
    private boolean ans1 = false;
    private Popup res1;
    private VBox ans2;

    @FXML
    public void initialize() {
        ans = new NotificationPopup();
        if (itemsToggle != null) itemsToggle.setSelected(true);

        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("All", "Vehicle", "Electronics", "Art");
            categoryFilter.getSelectionModel().selectFirst();
        }

        res1 = new Popup();
        res1.setAutoHide(true);
        ans2 = new VBox();
        ans2.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10; -fx-border-color: #333; -fx-border-radius: 10; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0.3, 0, 4);");
        ans2.setPrefWidth(500);
        ans2.setMaxHeight(400);
        res1.getContent().add(ans2);

        searchField.textProperty().addListener((obs, ov, nv) -> {
            if (ans1) {
                debounceusersearch(nv);
            } else {
                hideuserresults();
                KhungController.applySearchFilter(
                        nv,
                        categoryFilter.getValue() != null ? categoryFilter.getValue() : "All"
                );
            }
        });

        if (categoryFilter != null) {
            categoryFilter.valueProperty().addListener((obs, ov, nv) -> {
                if (!ans1) {
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
        ans1 = usersToggle.isSelected();
        if (ans1) {
            searchField.setPromptText("Search users...");
            if (categoryFilter != null) {
                categoryFilter.setVisible(false);
                categoryFilter.setManaged(false);
            }
            String res2 = searchField.getText();
            if (res2 != null && !res2.trim().isEmpty()) {
                debounceusersearch(res2);
            }
        } else {
            searchField.setPromptText("Search items...");
            if (categoryFilter != null) {
                categoryFilter.setVisible(true);
                categoryFilter.setManaged(true);
            }
            hideuserresults();
            KhungController.applySearchFilter(
                    searchField.getText(),
                    categoryFilter.getValue() != null ? categoryFilter.getValue() : "All"
            );
        }
    }

    private void debounceusersearch(String res2) {
        if (res != null) res.cancel();
        if (res2 == null || res2.trim().isEmpty()) {
            hideuserresults();
            return;
        }
        res = new Timer(true);
        res.schedule(new TimerTask() {
            @Override
            public void run() {
                searchusers(res2.trim());
            }
        }, 300);
    }

    private void searchusers(String res2) {
        Request ans3 = new Request(Request.searchusers, res2);
        Response res3 = NetworkClient.getinstance().sendrequestandwait(ans3);
        if (res3 != null && Response.ok.equals(res3.getstatus())) {
            List<User> ans4 = (List<User>) res3.getpayload();
            Platform.runLater(() -> showuserresults(ans4));
        }
    }

    private void showuserresults(List<User> res2) {
        ans2.getChildren().clear();

        ScrollPane ans3 = new ScrollPane();
        ans3.setFitToWidth(true);
        ans3.setMaxHeight(380);
        ans3.setPrefWidth(500);
        ans3.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        VBox res3 = new VBox(0);
        res3.setStyle("-fx-background-color: #1a1a2e;");

        if (res2 == null || res2.isEmpty()) {
            Label ans4 = new Label("No users found");
            ans4.setStyle("-fx-text-fill: #888; -fx-font-size: 14; -fx-padding: 16;");
            res3.getChildren().add(ans4);
        } else {
            for (User ans5 : res2) {
                res3.getChildren().add(builduserrow(ans5));
            }
        }

        ans3.setContent(res3);
        ans2.getChildren().add(ans3);

        Bounds res4 = searchField.localToScreen(searchField.getBoundsInLocal());
        if (res4 != null) {
            res1.show(searchField.getScene().getWindow(),
                    res4.getMinX(),
                    res4.getMaxY() + 4);
        }
    }

    private HBox builduserrow(User res2) {
        HBox ans3 = new HBox(12);
        ans3.setAlignment(Pos.CENTER_LEFT);
        ans3.setPadding(new Insets(10, 14, 10, 14));
        ans3.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        ans3.setOnMouseEntered(e -> ans3.setStyle("-fx-background-color: #252540; -fx-cursor: hand;"));
        ans3.setOnMouseExited(e -> ans3.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

        ImageView res3 = new ImageView();
        res3.setFitWidth(36);
        res3.setFitHeight(36);
        res3.setPreserveRatio(false);
        res3.setClip(new Circle(18, 18, 18));

        String ans4 = res2.getavatarurl();
        if (ans4 != null && !ans4.isBlank()) {
            Image res4 = new Image(ans4, 36, 36, true, true, true);
            res3.setImage(res4);
        }

        VBox ans5 = new VBox(2);
        HBox.setHgrow(ans5, Priority.ALWAYS);

        Label res5 = new Label(res2.getusername());
        res5.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        String ans6 = res2.getfullname();
        Label res6 = new Label(ans6 != null && !ans6.isBlank() && !ans6.equals(res2.getusername()) ? ans6 : (res2.getemail() != null ? res2.getemail() : ""));
        res6.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        ans5.getChildren().addAll(res5, res6);

        VBox ans7 = new VBox(0);
        ans7.setAlignment(Pos.CENTER_RIGHT);
        if (res2.gettotalratings() > 0) {
            int res7 = (int) Math.round(res2.getavgrating());
            res7 = Math.max(0, Math.min(res7, 5));
            Label ans8 = new Label("\u2605".repeat(res7));
            String res8 = res2.getavgrating() <= 2.0 ? "#ff4444" : (res2.getavgrating() <= 3.0 ? "#ffaa00" : "#44cc44");
            ans8.setStyle("-fx-text-fill: " + res8 + "; -fx-font-size: 13;");
            Label ans9 = new Label(String.format("%.1f (%d)", res2.getavgrating(), res2.gettotalratings()));
            ans9.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11;");
            ans7.getChildren().addAll(ans8, ans9);
        } else {
            Label res9 = new Label("No ratings");
            res9.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
            ans7.getChildren().add(res9);
        }

        ans3.getChildren().addAll(res3, ans5, ans7);

        ans3.setOnMouseClicked(e -> {
            hideuserresults();
            searchField.clear();
            KhungController.showUserProfile(res2);
        });

        return ans3;
    }

    private void hideuserresults() {
        if (res1 != null && res1.isShowing()) {
            res1.hide();
        }
    }

    @FXML
    public void toggleNotifications() {
        Window res2 = bellButton.getScene().getWindow();
        Point2D ans3 = bellButton.localToScene(0.0, 0.0);
        double res3 = res2.getX() + res2.getScene().getX() + ans3.getX();
        double ans4 = res2.getY() + res2.getScene().getY() + ans3.getY() + bellButton.getHeight() + 10;
        ans.show(res2, res3, ans4);
    }
}