package com.auction.client.ui.Main;

import com.auction.client.network.NetworkClient;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AdminDashboardController {
  @FXML private TableView<User> usertable;
  @FXML private TableColumn<User, String> colusername, colemail, colrole, colstatus, colrating;
  @FXML private Button btnban, btnunban;
  @FXML private TableView<Item> pendingtable;
  @FXML private TableColumn<Item, String> colitemname, colitemseller, colitemprice, colitemcategory;
  @FXML private Button btnapprove, btnreject;
  @FXML private ComboBox<String> ratingfilter;
  @FXML private javafx.scene.chart.PieChart statuschart;
  @FXML private javafx.scene.chart.BarChart<String, Number> categorychart;

  private ObservableList<User> userlist = FXCollections.observableArrayList();
  private FilteredList<User> filtereduserlist;
  private ObservableList<Item> pendinglist = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colusername.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getUsername()));
    colemail.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getEmail()));
    colrole.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getRole().toString()));
    colstatus.setCellValueFactory(r -> {
      User u = r.getValue();
      String s = u.isLocked() ? "LOCKED" : (u.getAvgRating() < 2.0 && u.getTotalRatings() >= 3 ? "LOW REP" : "active");
      return new SimpleStringProperty(s);
    });
    colrating.setCellValueFactory(r -> {
      User u = r.getValue();
      if (u.getTotalRatings() <= 0) return new SimpleStringProperty("N/A");
      String sentiment = u.getAvgRating() <= 2.0 ? "Negative" : (u.getAvgRating() <= 3.0 ? "Neutral" : "Positive");
      return new SimpleStringProperty(String.format("%.1f (%d) %s", u.getAvgRating(), u.getTotalRatings(), sentiment));
    });
    ratingfilter.getItems().addAll("All", "Positive", "Neutral", "Negative", "No Rating");
    ratingfilter.setValue("All");
    filtereduserlist = new FilteredList<>(userlist, u -> true);
    usertable.setItems(filtereduserlist);

    colitemname.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getName()));
    colitemseller.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getSellerUsername()));
    colitemprice.setCellValueFactory(r -> new SimpleStringProperty(String.format("%,.0f$", r.getValue().getStartingPrice())));
    colitemcategory.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getCategory()));
    pendingtable.setItems(pendinglist);

    loadUsers(); loadPendingItems(); loadStats();
  }

  private void asyncRequest(Request req, Consumer<Response> callback) {
    Thread t = new Thread(() -> { Response res = NetworkClient.getInstance().sendRequestAndWait(req); if (res != null) callback.accept(res); });
    t.setDaemon(true); t.start();
  }

  private void loadUsers() {
    asyncRequest(new Request(Request.GET_ALL_USERS, null), res -> {
      if (Response.OK.equals(res.getStatus())) {
        List<User> users = (List<User>) res.getPayload();
        if (users != null) Platform.runLater(() -> { userlist.clear(); userlist.addAll(users); });
      }
    });
  }

  private void loadPendingItems() {
    asyncRequest(new Request(Request.GET_PENDING_ITEMS, null), res -> {
      if (Response.OK.equals(res.getStatus())) {
        List<Item> items = (List<Item>) res.getPayload();
        if (items != null) Platform.runLater(() -> { pendinglist.clear(); pendinglist.addAll(items); });
      }
    });
  }

  private void loadStats() {
    asyncRequest(new Request("get_status_stats", null), res -> {
      if (Response.OK.equals(res.getStatus())) {
        java.util.HashMap<String, Integer> map = (java.util.HashMap<String, Integer>) res.getPayload();
        Platform.runLater(() -> {
          if (statuschart != null) {
            statuschart.getData().clear();
            for (java.util.Map.Entry<String, Integer> e : map.entrySet())
              statuschart.getData().add(new javafx.scene.chart.PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()));
          }
        });
      }
    });
    asyncRequest(new Request("get_category_stats", null), res -> {
      if (Response.OK.equals(res.getStatus())) {
        java.util.HashMap<String, Double> map = (java.util.HashMap<String, Double>) res.getPayload();
        Platform.runLater(() -> {
          if (categorychart != null) {
            categorychart.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Revenue");
            for (java.util.Map.Entry<String, Double> e : map.entrySet())
              series.getData().add(new javafx.scene.chart.XYChart.Data<>(e.getKey(), e.getValue()));
            categorychart.getData().add(series);
          }
        });
      }
    });
  }

  @FXML
  private void handleBan(ActionEvent event) {
    User user = usertable.getSelectionModel().getSelectedItem();
    if (user == null) return;
    asyncRequest(new Request(Request.LOCK_USER, user.getUsername()), res -> {
      if (Response.OK.equals(res.getStatus())) { user.setLocked(true); Platform.runLater(() -> usertable.refresh()); }
    });
  }

  @FXML
  private void handleUnban(ActionEvent event) {
    User user = usertable.getSelectionModel().getSelectedItem();
    if (user == null) return;
    asyncRequest(new Request(Request.UNLOCK_USER, user.getUsername()), res -> {
      if (Response.OK.equals(res.getStatus())) { user.setLocked(false); Platform.runLater(() -> usertable.refresh()); }
    });
  }

  @FXML
  private void handlePromoteAdmin(ActionEvent event) {
    User user = usertable.getSelectionModel().getSelectedItem();
    if (user == null) return;
    boolean isAdmin = user.getRole() == com.auction.shared.UserRole.ADMIN;
    String title = isAdmin ? "Demote from Admin" : "Promote to Admin";
    String question = (isAdmin ? "demote '" : "promote '") + user.getUsername() + (isAdmin ? "' back to Bidder?" : "' to Admin?");
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle(title); confirm.setHeaderText(null); confirm.setContentText("Are you sure you want to " + question);
    if (confirm.showAndWait().filter(b -> b == javafx.scene.control.ButtonType.OK).isPresent()) {
      String newRole = isAdmin ? com.auction.shared.UserRole.BIDDER.name() : com.auction.shared.UserRole.ADMIN.name();
      asyncRequest(new Request(Request.PROMOTE_ADMIN, user.getUsername() + ":" + newRole), res ->
          Platform.runLater(() -> {
            if (Response.OK.equals(res.getStatus())) {
              showAlert(Alert.AlertType.INFORMATION, "Success", user.getUsername() + (isAdmin ? " is no longer an Admin." : " is now an Admin."));
              loadUsers();
            } else showAlert(Alert.AlertType.ERROR, "Failed", "Could not change role.");
          }));
    }
  }

  @FXML
  private void handleApprove(ActionEvent event) {
    Item item = pendingtable.getSelectionModel().getSelectedItem();
    if (item == null) return;
    asyncRequest(new Request(Request.APPROVE_ITEM, item.getId()), res -> {
      if (Response.OK.equals(res.getStatus()))
        Platform.runLater(() -> { pendinglist.remove(item); showAlert(Alert.AlertType.INFORMATION, "Approved", "Item '" + item.getName() + "' is now live."); loadStats(); });
    });
  }

  @FXML
  private void handleReject(ActionEvent event) {
    Item item = pendingtable.getSelectionModel().getSelectedItem();
    if (item == null) return;
    asyncRequest(new Request(Request.REJECT_ITEM, item.getId()), res -> {
      if (Response.OK.equals(res.getStatus()))
        Platform.runLater(() -> { pendinglist.remove(item); showAlert(Alert.AlertType.INFORMATION, "Rejected", "Item '" + item.getName() + "' has been rejected."); loadStats(); });
    });
  }

  @FXML
  private void handleFilterChange(ActionEvent event) {
    String val = ratingfilter.getValue();
    if (val == null || val.equals("All")) filtereduserlist.setPredicate(u -> true);
    else if (val.equals("Positive")) filtereduserlist.setPredicate(u -> u.getTotalRatings() > 0 && u.getAvgRating() > 3.0);
    else if (val.equals("Neutral")) filtereduserlist.setPredicate(u -> u.getTotalRatings() > 0 && u.getAvgRating() > 2.0 && u.getAvgRating() <= 3.0);
    else if (val.equals("Negative")) filtereduserlist.setPredicate(u -> u.getTotalRatings() > 0 && u.getAvgRating() <= 2.0);
    else if (val.equals("No Rating")) filtereduserlist.setPredicate(u -> u.getTotalRatings() <= 0);
  }

  @FXML private void handleRefreshPending(ActionEvent event) { loadPendingItems(); loadUsers(); loadStats(); }

  private void showAlert(Alert.AlertType type, String title, String content) {
    Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
  }
}
