package com.auction.client.ui.Main;

import com.auction.client.network.NetworkClient;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;
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
  @FXML private TableColumn<User, String> colusername;
  @FXML private TableColumn<User, String> colemail;
  @FXML private TableColumn<User, String> colrole;
  @FXML private TableColumn<User, String> colstatus;
  @FXML private TableColumn<User, String> colrating;
  @FXML private Button btnban;
  @FXML private Button btnunban;

  @FXML private TableView<Item> pendingtable;
  @FXML private TableColumn<Item, String> colitemname;
  @FXML private TableColumn<Item, String> colitemseller;
  @FXML private TableColumn<Item, String> colitemprice;
  @FXML private TableColumn<Item, String> colitemcategory;
  @FXML private Button btnapprove;
  @FXML private Button btnreject;

  @FXML private ComboBox<String> ratingfilter;
  @FXML private javafx.scene.chart.PieChart statuschart;
  @FXML private javafx.scene.chart.BarChart<String, Number> categorychart;

  private ObservableList<User> userlist = FXCollections.observableArrayList();
  private FilteredList<User> filtereduserlist;
  private ObservableList<Item> pendinglist = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colusername.setCellValueFactory(res -> new SimpleStringProperty(res.getValue().getUsername()));
    colemail.setCellValueFactory(res -> new SimpleStringProperty(res.getValue().getEmail()));
    colrole.setCellValueFactory(
        res -> new SimpleStringProperty(res.getValue().getRole().toString()));
    colstatus.setCellValueFactory(
        res -> {
          User ans = res.getValue();
          String ans1 =
              ans.isLocked()
                  ? "LOCKED"
                  : (ans.getAvgRating() < 2.0 && ans.getTotalRatings() >= 3 ? "LOW REP" : "active");
          return new SimpleStringProperty(ans1);
        });
    colrating.setCellValueFactory(
        res -> {
          User ans = res.getValue();
          if (ans.getTotalRatings() <= 0) return new SimpleStringProperty("N/A");
          String ans1 =
              ans.getAvgRating() <= 2.0
                  ? "Negative"
                  : (ans.getAvgRating() <= 3.0 ? "Neutral" : "Positive");
          String ans2 =
              String.format("%.1f (%d) %s", ans.getAvgRating(), ans.getTotalRatings(), ans1);
          return new SimpleStringProperty(ans2);
        });
    ratingfilter.getItems().addAll("All", "Positive", "Neutral", "Negative", "No Rating");
    ratingfilter.setValue("All");
    filtereduserlist = new FilteredList<>(userlist, res1 -> true);
    usertable.setItems(filtereduserlist);

    colitemname.setCellValueFactory(res -> new SimpleStringProperty(res.getValue().getName()));
    colitemseller.setCellValueFactory(
        res -> new SimpleStringProperty(res.getValue().getSellerUsername()));
    colitemprice.setCellValueFactory(
        res ->
            new SimpleStringProperty(String.format("%,.0f$", res.getValue().getStartingPrice())));
    colitemcategory.setCellValueFactory(
        res -> new SimpleStringProperty(res.getValue().getCategory()));
    pendingtable.setItems(pendinglist);

    loadUsers();
    loadPendingItems();
    loadStats();
  }

  private void loadUsers() {
    new Thread(
            () -> {
              Request res = new Request(Request.GET_ALL_USERS, null);
              Response res1 = NetworkClient.getInstance().sendRequestAndWait(res);
              if (res1 != null && Response.OK.equals(res1.getStatus())) {
                List<User> ans = (List<User>) res1.getPayload();
                if (ans != null)
                  javafx.application.Platform.runLater(
                      () -> {
                        userlist.clear();
                        userlist.addAll(ans);
                      });
              }
            })
        .start();
  }

  private void loadPendingItems() {
    new Thread(
            () -> {
              Request res = new Request(Request.GET_PENDING_ITEMS, null);
              Response res1 = NetworkClient.getInstance().sendRequestAndWait(res);
              if (res1 != null && Response.OK.equals(res1.getStatus())) {
                List<Item> ans = (List<Item>) res1.getPayload();
                if (ans != null)
                  javafx.application.Platform.runLater(
                      () -> {
                        pendinglist.clear();
                        pendinglist.addAll(ans);
                      });
              }
            })
        .start();
  }

  private void loadStats() {
    new Thread(
            () -> {
              Request res = new Request("get_status_stats", null);
              Response ans = NetworkClient.getInstance().sendRequestAndWait(res);
              if (ans != null && Response.OK.equals(ans.getStatus())) {
                java.util.HashMap<String, Integer> res1 =
                    (java.util.HashMap<String, Integer>) ans.getPayload();
                javafx.application.Platform.runLater(
                    () -> {
                      if (statuschart != null) {
                        statuschart.getData().clear();
                        for (java.util.Map.Entry<String, Integer> ans1 : res1.entrySet()) {
                          statuschart
                              .getData()
                              .add(
                                  new javafx.scene.chart.PieChart.Data(
                                      ans1.getKey() + " (" + ans1.getValue() + ")",
                                      ans1.getValue()));
                        }
                      }
                    });
              }

              Request res2 = new Request("get_category_stats", null);
              Response ans2 = NetworkClient.getInstance().sendRequestAndWait(res2);
              if (ans2 != null && Response.OK.equals(ans2.getStatus())) {
                java.util.HashMap<String, Double> res3 =
                    (java.util.HashMap<String, Double>) ans2.getPayload();
                javafx.application.Platform.runLater(
                    () -> {
                      if (categorychart != null) {
                        categorychart.getData().clear();
                        javafx.scene.chart.XYChart.Series<String, Number> ans3 =
                            new javafx.scene.chart.XYChart.Series<>();
                        ans3.setName("Revenue");
                        for (java.util.Map.Entry<String, Double> res4 : res3.entrySet()) {
                          ans3.getData()
                              .add(
                                  new javafx.scene.chart.XYChart.Data<>(
                                      res4.getKey(), res4.getValue()));
                        }
                        categorychart.getData().add(ans3);
                      }
                    });
              }
            })
        .start();
  }

  @FXML
  private void handleBan(ActionEvent event) {
    User res = usertable.getSelectionModel().getSelectedItem();
    if (res == null) return;
    new Thread(
            () -> {
              Request res1 = new Request(Request.LOCK_USER, res.getUsername());
              Response res2 = NetworkClient.getInstance().sendRequestAndWait(res1);
              if (res2 != null && Response.OK.equals(res2.getStatus())) {
                res.setLocked(true);
                javafx.application.Platform.runLater(() -> usertable.refresh());
              }
            })
        .start();
  }

  @FXML
  private void handleUnban(ActionEvent event) {
    User res = usertable.getSelectionModel().getSelectedItem();
    if (res == null) return;
    new Thread(
            () -> {
              Request res1 = new Request(Request.UNLOCK_USER, res.getUsername());
              Response res2 = NetworkClient.getInstance().sendRequestAndWait(res1);
              if (res2 != null && Response.OK.equals(res2.getStatus())) {
                res.setLocked(false);
                javafx.application.Platform.runLater(() -> usertable.refresh());
              }
            })
        .start();
  }

  @FXML
  private void handlePromoteAdmin(ActionEvent event) {
    User res = usertable.getSelectionModel().getSelectedItem();
    if (res == null) return;
    boolean ans = res.getRole() == com.auction.shared.UserRole.ADMIN;
    String ans1 = ans ? "Demote from Admin" : "Promote to Admin";
    String ans2 =
        ans
            ? "demote '" + res.getUsername() + "' back to Bidder?"
            : "promote '" + res.getUsername() + "' to Admin?";
    Alert res1 = new Alert(Alert.AlertType.CONFIRMATION);
    res1.setTitle(ans1);
    res1.setHeaderText(null);
    res1.setContentText("Are you sure you want to " + ans2);
    java.util.Optional<javafx.scene.control.ButtonType> res2 = res1.showAndWait();
    if (res2.isPresent() && res2.get() == javafx.scene.control.ButtonType.OK) {
      String ans3 =
          ans
              ? com.auction.shared.UserRole.BIDDER.name()
              : com.auction.shared.UserRole.ADMIN.name();
      new Thread(
              () -> {
                Request res3 = new Request(Request.PROMOTE_ADMIN, res.getUsername() + ":" + ans3);
                Response res4 = NetworkClient.getInstance().sendRequestAndWait(res3);
                javafx.application.Platform.runLater(
                    () -> {
                      if (res4 != null && Response.OK.equals(res4.getStatus())) {
                        String ans4 = ans ? " is no longer an Admin." : " is now an Admin.";
                        showAlert(Alert.AlertType.INFORMATION, "Success", res.getUsername() + ans4);
                        loadUsers();
                      } else {
                        showAlert(Alert.AlertType.ERROR, "Failed", "Could not change role.");
                      }
                    });
              })
          .start();
    }
  }

  @FXML
  private void handleApprove(ActionEvent event) {
    Item res = pendingtable.getSelectionModel().getSelectedItem();
    if (res == null) return;
    new Thread(
            () -> {
              Request res1 = new Request(Request.APPROVE_ITEM, res.getId());
              Response res2 = NetworkClient.getInstance().sendRequestAndWait(res1);
              if (res2 != null && Response.OK.equals(res2.getStatus())) {
                javafx.application.Platform.runLater(
                    () -> {
                      pendinglist.remove(res);
                      showAlert(
                          Alert.AlertType.INFORMATION,
                          "Approved",
                          "Item '" + res.getName() + "' is now live.");
                      loadStats();
                    });
              }
            })
        .start();
  }

  @FXML
  private void handleReject(ActionEvent event) {
    Item res = pendingtable.getSelectionModel().getSelectedItem();
    if (res == null) return;
    new Thread(
            () -> {
              Request res1 = new Request(Request.REJECT_ITEM, res.getId());
              Response res2 = NetworkClient.getInstance().sendRequestAndWait(res1);
              if (res2 != null && Response.OK.equals(res2.getStatus())) {
                javafx.application.Platform.runLater(
                    () -> {
                      pendinglist.remove(res);
                      showAlert(
                          Alert.AlertType.INFORMATION,
                          "Rejected",
                          "Item '" + res.getName() + "' has been rejected.");
                      loadStats();
                    });
              }
            })
        .start();
  }

  @FXML
  private void handleFilterChange(ActionEvent event) {
    String res = ratingfilter.getValue();
    if (res == null || res.equals("All")) {
      filtereduserlist.setPredicate(ans -> true);
    } else if (res.equals("Positive")) {
      filtereduserlist.setPredicate(ans -> ans.getTotalRatings() > 0 && ans.getAvgRating() > 3.0);
    } else if (res.equals("Neutral")) {
      filtereduserlist.setPredicate(
          ans ->
              ans.getTotalRatings() > 0 && ans.getAvgRating() > 2.0 && ans.getAvgRating() <= 3.0);
    } else if (res.equals("Negative")) {
      filtereduserlist.setPredicate(ans -> ans.getTotalRatings() > 0 && ans.getAvgRating() <= 2.0);
    } else if (res.equals("No Rating")) {
      filtereduserlist.setPredicate(ans -> ans.getTotalRatings() <= 0);
    }
  }

  @FXML
  private void handleRefreshPending(ActionEvent event) {
    loadPendingItems();
    loadUsers();
    loadStats();
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
    Alert res = new Alert(type);
    res.setTitle(title);
    res.setHeaderText(null);
    res.setContentText(content);
    res.showAndWait();
  }
}
