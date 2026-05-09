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
import javafx.scene.Node;
import javafx.scene.control.*;
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

  private NotificationPopup notifpopup;
  private Timer debouncetimer;
  private boolean usermode = false;
  private final UserSearchResultsPopup usersearchresults = new UserSearchResultsPopup();
  private ContextMenu autocompletemenu = new ContextMenu();

  @FXML
  public void initialize() {
    notifpopup = new NotificationPopup();
    if (itemsToggle != null) {
      itemsToggle.setSelected(true);
    }
    if (categoryFilter != null) {
      categoryFilter.getItems().addAll("All", "Vehicle", "Electronics", "Art");
      categoryFilter.getSelectionModel().selectFirst();
    }
    // REFACTOR: extract autocomplete logic into a separate service class to keep controller clean
    searchField.textProperty().addListener((obs, oldval, newval) -> {
      if (usermode) {
        debounceusersearch(newval);
      } else {
        hideresults();
        KhungController.applySearchFilter(newval, getcat(), 0, Double.MAX_VALUE);
        if (newval != null && !newval.isEmpty()) {
          Request req = new Request(Request.AUTOCOMPLETE, newval);
          Thread thread = new Thread(() -> {
            Response res = NetworkClient.getInstance().sendRequestAndWait(req);
            if (res != null && Response.OK.equals(res.getStatus())) {
              @SuppressWarnings("unchecked")
              List<String> ans = (List<String>) res.getPayload();
              Platform.runLater(() -> {
                showautocomplete(ans);
              });
            }
          });
          thread.setDaemon(true);
          thread.start();
        } else {
          autocompletemenu.hide();
        }
      }
    });
    if (categoryFilter != null) {
      categoryFilter.valueProperty().addListener((obs, oldval, newval) -> {
        if (!usermode) {
          String ans = newval != null ? newval : "All";
          KhungController.applySearchFilter(searchField.getText(), ans, 0, Double.MAX_VALUE);
        }
      });
    }
  }

  private void showautocomplete(List<String> list) {
    autocompletemenu.getItems().clear();
    for (String str : list) {
      MenuItem item = new MenuItem(str);
      item.setOnAction(e -> {
        searchField.setText(str);
        autocompletemenu.hide();
      });
      autocompletemenu.getItems().add(item);
    }
    if (!autocompletemenu.getItems().isEmpty()) {
      javafx.geometry.Bounds bounds = searchField.localToScreen(searchField.getBoundsInLocal());
      autocompletemenu.show(searchField, bounds.getMinX(), bounds.getMaxY());
    } else {
      autocompletemenu.hide();
    }
  }

  @FXML
  public void onSearchModeChanged() {
    usermode = usersToggle.isSelected();
    searchField.setPromptText(usermode ? "Search users..." : "Search items...");
    setfiltercontrolsvisible(!usermode);
    if (usermode) {
      String kw = searchField.getText();
      if (kw != null && !kw.trim().isEmpty()) {
        debounceusersearch(kw);
      }
    } else {
      hideresults();
      KhungController.applySearchFilter(searchField.getText(), getcat(), 0, Double.MAX_VALUE);
    }
  }

  private void setfiltercontrolsvisible(boolean visible) {
    Node[] nodes = {categoryFilter, minpricefield, maxpricefield, filterButton};
    for (Node n : nodes) {
      if (n != null) {
        n.setVisible(visible);
        n.setManaged(visible);
      }
    }
  }

  private String getcat() {
    String ans = categoryFilter != null && categoryFilter.getValue() != null ? categoryFilter.getValue() : "All";
    return ans;
  }

  private void debounceusersearch(String kw) {
    if (debouncetimer != null) {
      debouncetimer.cancel();
    }
    if (kw == null || kw.trim().isEmpty()) {
      hideresults();
      return;
    }
    debouncetimer = new Timer(true);
    debouncetimer.schedule(new TimerTask() {
      @Override
      public void run() {
        searchusers(kw.trim());
      }
    }, 300);
  }

  private void searchusers(String kw) {
    Request req = new Request(Request.SEARCH_USERS, kw);
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && Response.OK.equals(res.getStatus())) {
      @SuppressWarnings("unchecked")
      List<User> ans = (List<User>) res.getPayload();
      Platform.runLater(() -> {
        showuserresults(ans);
      });
    }
  }

  private void showuserresults(List<User> ans) {
    usersearchresults.showUnder(
            searchField,
            ans,
            u -> {
              hideresults();
              searchField.clear();
              KhungController.showUserProfile(u);
            });
  }

  private void hideresults() {
    usersearchresults.hide();
  }

  @FXML
  public void applyFilter() {
    String kw = searchField.getText();
    String cat = getcat();
    double min = 0;
    double max = Double.MAX_VALUE;
    try {
      if (minpricefield != null && !minpricefield.getText().isBlank()) {
        min = Double.parseDouble(minpricefield.getText());
      }
    } catch (Exception e) {
    }
    try {
      if (maxpricefield != null && !maxpricefield.getText().isBlank()) {
        max = Double.parseDouble(maxpricefield.getText());
      }
    } catch (Exception e) {
    }
    KhungController.applySearchFilter(kw, cat, min, max);
  }

  @FXML
  public void toggleNotifications() {
    Window win = bellButton.getScene().getWindow();
    Point2D pos = bellButton.localToScene(0.0, 0.0);
    double x = win.getX() + win.getScene().getX() + pos.getX();
    double y = win.getY() + win.getScene().getY() + pos.getY() + bellButton.getHeight() + 10;
    notifpopup.show(win, x, y);
  }
}