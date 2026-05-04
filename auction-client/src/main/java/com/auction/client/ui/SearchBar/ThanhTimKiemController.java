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
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Window;

public class ThanhTimKiemController {
  @FXML private TextField searchField;
  @FXML private ComboBox<String> categoryFilter;
  @FXML private Button bellButton;
  @FXML private ToggleButton itemsToggle, usersToggle;
  @FXML private ToggleGroup searchModeGroup;
  @FXML private TextField minpricefield, maxpricefield;
  @FXML private Button filterButton;

  private NotificationPopup notifPopup;
  private Timer debounceTimer;
  private boolean userMode = false;
  private Popup resultsPopup;
  private VBox resultsBox;

  @FXML
  public void initialize() {
    notifPopup = new NotificationPopup();
    if (itemsToggle != null) itemsToggle.setSelected(true);
    if (categoryFilter != null) {
      categoryFilter.getItems().addAll("All", "Vehicle", "Electronics", "Art");
      categoryFilter.getSelectionModel().selectFirst();
    }
    resultsPopup = new Popup();
    resultsPopup.setAutoHide(true);
    resultsBox = new VBox();
    resultsBox.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10; -fx-border-color: #333; -fx-border-radius: 10; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0.3, 0, 4);");
    resultsBox.setPrefWidth(500); resultsBox.setMaxHeight(400);
    resultsPopup.getContent().add(resultsBox);
    searchField.textProperty().addListener((obs, ov, nv) -> {
      if (userMode) debounceUserSearch(nv);
      else { hideResults(); KhungController.applySearchFilter(nv, getCat(), 0, Double.MAX_VALUE); }
    });
    if (categoryFilter != null)
      categoryFilter.valueProperty().addListener((obs, ov, nv) -> {
        if (!userMode) KhungController.applySearchFilter(searchField.getText(), nv != null ? nv : "All", 0, Double.MAX_VALUE);
      });
  }

  @FXML
  public void onSearchModeChanged() {
    userMode = usersToggle.isSelected();
    searchField.setPromptText(userMode ? "Search users..." : "Search items...");
    setFilterControlsVisible(!userMode);
    if (userMode) {
      String kw = searchField.getText();
      if (kw != null && !kw.trim().isEmpty()) debounceUserSearch(kw);
    } else {
      hideResults();
      KhungController.applySearchFilter(searchField.getText(), getCat(), 0, Double.MAX_VALUE);
    }
  }

  private void setFilterControlsVisible(boolean visible) {
    for (javafx.scene.Node n : new javafx.scene.Node[]{categoryFilter, minpricefield, maxpricefield, filterButton})
      if (n != null) { n.setVisible(visible); n.setManaged(visible); }
  }

  private String getCat() {
    return categoryFilter != null && categoryFilter.getValue() != null ? categoryFilter.getValue() : "All";
  }

  private void debounceUserSearch(String kw) {
    if (debounceTimer != null) debounceTimer.cancel();
    if (kw == null || kw.trim().isEmpty()) { hideResults(); return; }
    debounceTimer = new Timer(true);
    debounceTimer.schedule(new TimerTask() {
      @Override public void run() { searchUsers(kw.trim()); }
    }, 300);
  }

  private void searchUsers(String kw) {
    Response res = NetworkClient.getInstance().sendRequestAndWait(new Request(Request.SEARCH_USERS, kw));
    if (res != null && Response.OK.equals(res.getStatus()))
      Platform.runLater(() -> showUserResults((List<User>) res.getPayload()));
  }

  private void showUserResults(List<User> users) {
    resultsBox.getChildren().clear();
    ScrollPane scroll = new ScrollPane();
    scroll.setFitToWidth(true); scroll.setMaxHeight(380); scroll.setPrefWidth(500);
    scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
    VBox content = new VBox(0);
    content.setStyle("-fx-background-color: #1a1a2e;");
    if (users == null || users.isEmpty()) {
      Label empty = new Label("No users found");
      empty.setStyle("-fx-text-fill: #888; -fx-font-size: 14; -fx-padding: 16;");
      content.getChildren().add(empty);
    } else {
      for (User u : users) content.getChildren().add(buildUserRow(u));
    }
    scroll.setContent(content);
    resultsBox.getChildren().add(scroll);
    Bounds b = searchField.localToScreen(searchField.getBoundsInLocal());
    if (b != null) resultsPopup.show(searchField.getScene().getWindow(), b.getMinX(), b.getMaxY() + 4);
  }

  private HBox buildUserRow(User u) {
    HBox row = new HBox(12);
    row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(10, 14, 10, 14));
    row.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
    row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #252540; -fx-cursor: hand;"));
    row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

    ImageView avatar = new ImageView();
    avatar.setFitWidth(36); avatar.setFitHeight(36); avatar.setPreserveRatio(false); avatar.setClip(new Circle(18, 18, 18));
    String avatarUrl = u.getAvatarUrl();
    if (avatarUrl != null && !avatarUrl.isBlank()) avatar.setImage(new Image(avatarUrl, 36, 36, true, true, true));

    Label nameLabel = new Label(u.getUsername());
    nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
    String fn = u.getFullName();
    Label subLabel = new Label(fn != null && !fn.isBlank() && !fn.equals(u.getUsername()) ? fn : (u.getEmail() != null ? u.getEmail() : ""));
    subLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
    VBox info = new VBox(2, nameLabel, subLabel);
    HBox.setHgrow(info, Priority.ALWAYS);

    VBox ratingBox = new VBox(0);
    ratingBox.setAlignment(Pos.CENTER_RIGHT);
    if (u.getTotalRatings() > 0) {
      int stars = Math.max(0, Math.min((int) Math.round(u.getAvgRating()), 5));
      String color = u.getAvgRating() <= 2.0 ? "#ff4444" : (u.getAvgRating() <= 3.0 ? "#ffaa00" : "#44cc44");
      Label starsLbl = new Label("\u2605".repeat(stars));
      starsLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13;");
      Label countLbl = new Label(String.format("%.1f (%d)", u.getAvgRating(), u.getTotalRatings()));
      countLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11;");
      ratingBox.getChildren().addAll(starsLbl, countLbl);
    } else {
      Label noRating = new Label("No ratings"); noRating.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
      ratingBox.getChildren().add(noRating);
    }
    row.getChildren().addAll(avatar, info, ratingBox);
    row.setOnMouseClicked(e -> { hideResults(); searchField.clear(); KhungController.showUserProfile(u); });
    return row;
  }

  private void hideResults() { if (resultsPopup != null && resultsPopup.isShowing()) resultsPopup.hide(); }

  @FXML
  public void applyFilter() {
    String kw = searchField.getText(), cat = getCat();
    double min = 0, max = Double.MAX_VALUE;
    try { if (minpricefield != null && !minpricefield.getText().isBlank()) min = Double.parseDouble(minpricefield.getText()); } catch (Exception e) {}
    try { if (maxpricefield != null && !maxpricefield.getText().isBlank()) max = Double.parseDouble(maxpricefield.getText()); } catch (Exception e) {}
    KhungController.applySearchFilter(kw, cat, min, max);
  }

  @FXML
  public void toggleNotifications() {
    Window win = bellButton.getScene().getWindow();
    Point2D pos = bellButton.localToScene(0.0, 0.0);
    double x = win.getX() + win.getScene().getX() + pos.getX();
    double y = win.getY() + win.getScene().getY() + pos.getY() + bellButton.getHeight() + 10;
    notifPopup.show(win, x, y);
  }
}
