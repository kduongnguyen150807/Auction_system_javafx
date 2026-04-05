package com.auction.client.ui.SearchBar;

import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.client.util.NotificationPopup;
import com.auction.shared.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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

public class ThanhTimKiemController {
  @FXML private TextField searchField;
  @FXML private ComboBox<String> categoryFilter;
  @FXML private Button bellButton;
  @FXML private ToggleButton itemsToggle;
  @FXML private ToggleButton usersToggle;
  @FXML private ToggleGroup searchModeGroup;
  @FXML private TextField minpricefield;
  @FXML private TextField maxpricefield;
  @FXML private Button filterButton;

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
    ans2.setStyle(
        "-fx-background-color: #1a1a2e; -fx-background-radius: 10; -fx-border-color: #333; -fx-border-radius: 10; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0.3, 0, 4);");
    ans2.setPrefWidth(500);
    ans2.setMaxHeight(400);
    res1.getContent().add(ans2);

    searchField
        .textProperty()
        .addListener(
            (obs, ov, nv) -> {
              if (ans1) {
                debounceUserSearch(nv);
              } else {
                hideUserResults();
                KhungController.applySearchFilter(
                    nv,
                    categoryFilter.getValue() != null ? categoryFilter.getValue() : "All",
                    0,
                    Double.MAX_VALUE);
              }
            });

    if (categoryFilter != null) {
      categoryFilter
          .valueProperty()
          .addListener(
              (obs, ov, nv) -> {
                if (!ans1) {
                  KhungController.applySearchFilter(
                      searchField.getText(), nv != null ? nv : "All", 0, Double.MAX_VALUE);
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
      if (minpricefield != null) {
        minpricefield.setVisible(false);
        minpricefield.setManaged(false);
      }
      if (maxpricefield != null) {
        maxpricefield.setVisible(false);
        maxpricefield.setManaged(false);
      }
      if (filterButton != null) {
        filterButton.setVisible(false);
        filterButton.setManaged(false);
      }
      String ans3 = searchField.getText();
      if (ans3 != null && !ans3.trim().isEmpty()) {
        debounceUserSearch(ans3);
      }
    } else {
      searchField.setPromptText("Search items...");
      if (categoryFilter != null) {
        categoryFilter.setVisible(true);
        categoryFilter.setManaged(true);
      }
      if (minpricefield != null) {
        minpricefield.setVisible(true);
        minpricefield.setManaged(true);
      }
      if (maxpricefield != null) {
        maxpricefield.setVisible(true);
        maxpricefield.setManaged(true);
      }
      if (filterButton != null) {
        filterButton.setVisible(true);
        filterButton.setManaged(true);
      }
      hideUserResults();
      KhungController.applySearchFilter(
          searchField.getText(),
          categoryFilter.getValue() != null ? categoryFilter.getValue() : "All",
          0,
          Double.MAX_VALUE);
    }
  }

  private void debounceUserSearch(String ans3) {
    if (res != null) res.cancel();
    if (ans3 == null || ans3.trim().isEmpty()) {
      hideUserResults();
      return;
    }
    res = new Timer(true);
    res.schedule(
        new TimerTask() {
          @Override
          public void run() {
            searchUsers(ans3.trim());
          }
        },
        300);
  }

  private void searchUsers(String ans3) {
    Request res2 = new Request(Request.SEARCH_USERS, ans3);
    Response ans4 = NetworkClient.getInstance().sendRequestAndWait(res2);
    if (ans4 != null && Response.OK.equals(ans4.getStatus())) {
      List<User> res3 = (List<User>) ans4.getPayload();
      Platform.runLater(() -> showUserResults(res3));
    }
  }

  private void showUserResults(List<User> ans3) {
    ans2.getChildren().clear();
    ScrollPane res2 = new ScrollPane();
    res2.setFitToWidth(true);
    res2.setMaxHeight(380);
    res2.setPrefWidth(500);
    res2.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
    VBox ans4 = new VBox(0);
    ans4.setStyle("-fx-background-color: #1a1a2e;");

    if (ans3 == null || ans3.isEmpty()) {
      Label res3 = new Label("No users found");
      res3.setStyle("-fx-text-fill: #888; -fx-font-size: 14; -fx-padding: 16;");
      ans4.getChildren().add(res3);
    } else {
      for (User res3 : ans3) {
        ans4.getChildren().add(buildUserRow(res3));
      }
    }
    res2.setContent(ans4);
    ans2.getChildren().add(res2);
    Bounds res3 = searchField.localToScreen(searchField.getBoundsInLocal());
    if (res3 != null) {
      res1.show(searchField.getScene().getWindow(), res3.getMinX(), res3.getMaxY() + 4);
    }
  }

  private HBox buildUserRow(User ans3) {
    HBox res2 = new HBox(12);
    res2.setAlignment(Pos.CENTER_LEFT);
    res2.setPadding(new Insets(10, 14, 10, 14));
    res2.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
    res2.setOnMouseEntered(e -> res2.setStyle("-fx-background-color: #252540; -fx-cursor: hand;"));
    res2.setOnMouseExited(
        e -> res2.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

    ImageView ans4 = new ImageView();
    ans4.setFitWidth(36);
    ans4.setFitHeight(36);
    ans4.setPreserveRatio(false);
    ans4.setClip(new Circle(18, 18, 18));
    String res3 = ans3.getAvatarUrl();
    if (res3 != null && !res3.isBlank()) {
      Image ans5 = new Image(res3, 36, 36, true, true, true);
      ans4.setImage(ans5);
    }

    VBox res4 = new VBox(2);
    HBox.setHgrow(res4, Priority.ALWAYS);
    Label ans5 = new Label(ans3.getUsername());
    ans5.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
    String res5 = ans3.getFullName();
    Label ans6 =
        new Label(
            res5 != null && !res5.isBlank() && !res5.equals(ans3.getUsername())
                ? res5
                : (ans3.getEmail() != null ? ans3.getEmail() : ""));
    ans6.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
    res4.getChildren().addAll(ans5, ans6);

    VBox res6 = new VBox(0);
    res6.setAlignment(Pos.CENTER_RIGHT);
    if (ans3.getTotalRatings() > 0) {
      int ans7 = (int) Math.round(ans3.getAvgRating());
      ans7 = Math.max(0, Math.min(ans7, 5));
      Label res7 = new Label("\u2605".repeat(ans7));
      String ans8 =
          ans3.getAvgRating() <= 2.0
              ? "#ff4444"
              : (ans3.getAvgRating() <= 3.0 ? "#ffaa00" : "#44cc44");
      res7.setStyle("-fx-text-fill: " + ans8 + "; -fx-font-size: 13;");
      Label res8 =
          new Label(String.format("%.1f (%d)", ans3.getAvgRating(), ans3.getTotalRatings()));
      res8.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11;");
      res6.getChildren().addAll(res7, res8);
    } else {
      Label ans7 = new Label("No ratings");
      ans7.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
      res6.getChildren().add(ans7);
    }
    res2.getChildren().addAll(ans4, res4, res6);
    res2.setOnMouseClicked(
        e -> {
          hideUserResults();
          searchField.clear();
          KhungController.showUserProfile(ans3);
        });
    return res2;
  }

  private void hideUserResults() {
    if (res1 != null && res1.isShowing()) {
      res1.hide();
    }
  }

  @FXML
  public void applyFilter() {
    String ans3 = searchField.getText();
    String res2 = null;
    if (categoryFilter != null) res2 = categoryFilter.getValue();
    double ans4 = 0;
    double res3 = Double.MAX_VALUE;
    try {
      if (minpricefield != null
          && minpricefield.getText() != null
          && !minpricefield.getText().isBlank()) {
        ans4 = Double.parseDouble(minpricefield.getText());
      }
    } catch (Exception e) {
    }
    try {
      if (maxpricefield != null
          && maxpricefield.getText() != null
          && !maxpricefield.getText().isBlank()) {
        res3 = Double.parseDouble(maxpricefield.getText());
      }
    } catch (Exception e) {
    }
    KhungController.applySearchFilter(ans3, res2, ans4, res3);
  }

  @FXML
  public void toggleNotifications() {
    Window ans3 = bellButton.getScene().getWindow();
    Point2D res2 = bellButton.localToScene(0.0, 0.0);
    double ans4 = ans3.getX() + ans3.getScene().getX() + res2.getX();
    double res3 = ans3.getY() + ans3.getScene().getY() + res2.getY() + bellButton.getHeight() + 10;
    ans.show(ans3, ans4, res3);
  }
}
