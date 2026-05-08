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
import javafx.geometry.Point2D;
import javafx.scene.control.*;
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
  private final UserSearchResultsPopup userSearchResults = new UserSearchResultsPopup();

  @FXML
  public void initialize() {
    notifPopup = new NotificationPopup();
    if (itemsToggle != null) itemsToggle.setSelected(true);
    if (categoryFilter != null) {
      categoryFilter.getItems().addAll("All", "Vehicle", "Electronics", "Art");
      categoryFilter.getSelectionModel().selectFirst();
    }
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
    if (res != null && Response.OK.equals(res.getStatus())) {
      @SuppressWarnings("unchecked")
      List<User> users = (List<User>) res.getPayload();
      Platform.runLater(() -> showUserResults(users));
    }
  }

  private void showUserResults(List<User> users) {
    userSearchResults.showUnder(
        searchField,
        users,
        u -> {
          hideResults();
          searchField.clear();
          KhungController.showUserProfile(u);
        });
  }

  private void hideResults() {
    userSearchResults.hide();
  }

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
