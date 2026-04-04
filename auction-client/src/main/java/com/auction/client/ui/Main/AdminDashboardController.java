package com.auction.client.ui.Main;

import com.auction.client.network.NetworkClient;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
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
import java.util.List;

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
        colusername.setCellValueFactory(res -> new SimpleStringProperty(res.getValue().getusername()));
        colemail.setCellValueFactory(res -> new SimpleStringProperty(res.getValue().getemail()));
        colrole.setCellValueFactory(res -> new SimpleStringProperty(res.getValue().getrole().toString()));
        colstatus.setCellValueFactory(res -> {
            User ans = res.getValue();
            String ans1 = ans.islocked() ? "LOCKED" : (ans.getavgrating() < 2.0 && ans.gettotalratings() >= 3 ? "LOW REP" : "active");
            return new SimpleStringProperty(ans1);
        });
        colrating.setCellValueFactory(res -> {
            User ans = res.getValue();
            if (ans.gettotalratings() <= 0) return new SimpleStringProperty("N/A");
            String ans1 = ans.getavgrating() <= 2.0 ? "Negative" : (ans.getavgrating() <= 3.0 ? "Neutral" : "Positive");
            String ans2 = String.format("%.1f (%d) %s", ans.getavgrating(), ans.gettotalratings(), ans1);
            return new SimpleStringProperty(ans2);
        });
        ratingfilter.getItems().addAll("All", "Positive", "Neutral", "Negative", "No Rating");
        ratingfilter.setValue("All");
        filtereduserlist = new FilteredList<>(userlist, res1 -> true);
        usertable.setItems(filtereduserlist);

        colitemname.setCellValueFactory(res -> new SimpleStringProperty(res.getValue().getname()));
        colitemseller.setCellValueFactory(res -> new SimpleStringProperty(res.getValue().getsellerusername()));
        colitemprice.setCellValueFactory(res -> new SimpleStringProperty(String.format("%,.0f$", res.getValue().getstartingprice())));
        colitemcategory.setCellValueFactory(res -> new SimpleStringProperty(res.getValue().getcategory()));
        pendingtable.setItems(pendinglist);

        loadusers();
        loadpendingitems();
        loadstats();
    }

    private void loadusers() {
        new Thread(() -> {
            Request res = new Request(Request.getallusers, null);
            Response res1 = NetworkClient.getinstance().sendrequestandwait(res);
            if (res1 != null && Response.ok.equals(res1.getstatus())) {
                List<User> ans = (List<User>) res1.getpayload();
                if (ans != null) javafx.application.Platform.runLater(() -> { userlist.clear(); userlist.addAll(ans); });
            }
        }).start();
    }

    private void loadpendingitems() {
        new Thread(() -> {
            Request res = new Request(Request.getpendingitems, null);
            Response res1 = NetworkClient.getinstance().sendrequestandwait(res);
            if (res1 != null && Response.ok.equals(res1.getstatus())) {
                List<Item> ans = (List<Item>) res1.getpayload();
                if (ans != null) javafx.application.Platform.runLater(() -> { pendinglist.clear(); pendinglist.addAll(ans); });
            }
        }).start();
    }

    private void loadstats() {
        new Thread(() -> {
            Request res = new Request("get_status_stats", null);
            Response ans = NetworkClient.getinstance().sendrequestandwait(res);
            if (ans != null && Response.ok.equals(ans.getstatus())) {
                java.util.HashMap<String, Integer> res1 = (java.util.HashMap<String, Integer>) ans.getpayload();
                javafx.application.Platform.runLater(() -> {
                    if (statuschart != null) {
                        statuschart.getData().clear();
                        for (java.util.Map.Entry<String, Integer> ans1 : res1.entrySet()) {
                            statuschart.getData().add(new javafx.scene.chart.PieChart.Data(ans1.getKey() + " (" + ans1.getValue() + ")", ans1.getValue()));
                        }
                    }
                });
            }

            Request res2 = new Request("get_category_stats", null);
            Response ans2 = NetworkClient.getinstance().sendrequestandwait(res2);
            if (ans2 != null && Response.ok.equals(ans2.getstatus())) {
                java.util.HashMap<String, Double> res3 = (java.util.HashMap<String, Double>) ans2.getpayload();
                javafx.application.Platform.runLater(() -> {
                    if (categorychart != null) {
                        categorychart.getData().clear();
                        javafx.scene.chart.XYChart.Series<String, Number> ans3 = new javafx.scene.chart.XYChart.Series<>();
                        ans3.setName("Revenue");
                        for (java.util.Map.Entry<String, Double> res4 : res3.entrySet()) {
                            ans3.getData().add(new javafx.scene.chart.XYChart.Data<>(res4.getKey(), res4.getValue()));
                        }
                        categorychart.getData().add(ans3);
                    }
                });
            }
        }).start();
    }

    @FXML
    private void handleban(ActionEvent event) {
        User res = usertable.getSelectionModel().getSelectedItem();
        if (res == null) return;
        new Thread(() -> {
            Request res1 = new Request(Request.lockuser, res.getusername());
            Response res2 = NetworkClient.getinstance().sendrequestandwait(res1);
            if (res2 != null && Response.ok.equals(res2.getstatus())) {
                res.setlocked(true);
                javafx.application.Platform.runLater(() -> usertable.refresh());
            }
        }).start();
    }

    @FXML
    private void handleunban(ActionEvent event) {
        User res = usertable.getSelectionModel().getSelectedItem();
        if (res == null) return;
        new Thread(() -> {
            Request res1 = new Request(Request.unlockuser, res.getusername());
            Response res2 = NetworkClient.getinstance().sendrequestandwait(res1);
            if (res2 != null && Response.ok.equals(res2.getstatus())) {
                res.setlocked(false);
                javafx.application.Platform.runLater(() -> usertable.refresh());
            }
        }).start();
    }

    @FXML
    private void handlepromoteadmin(ActionEvent event) {
        User res = usertable.getSelectionModel().getSelectedItem();
        if (res == null) return;
        boolean ans = res.getrole() == com.auction.shared.UserRole.ADMIN;
        String ans1 = ans ? "Demote from Admin" : "Promote to Admin";
        String ans2 = ans ? "demote '" + res.getusername() + "' back to Bidder?" : "promote '" + res.getusername() + "' to Admin?";
        Alert res1 = new Alert(Alert.AlertType.CONFIRMATION);
        res1.setTitle(ans1);
        res1.setHeaderText(null);
        res1.setContentText("Are you sure you want to " + ans2);
        java.util.Optional<javafx.scene.control.ButtonType> res2 = res1.showAndWait();
        if (res2.isPresent() && res2.get() == javafx.scene.control.ButtonType.OK) {
            String ans3 = ans ? com.auction.shared.UserRole.BIDDER.name() : com.auction.shared.UserRole.ADMIN.name();
            new Thread(() -> {
                Request res3 = new Request(Request.promoteadmin, res.getusername() + ":" + ans3);
                Response res4 = NetworkClient.getinstance().sendrequestandwait(res3);
                javafx.application.Platform.runLater(() -> {
                    if (res4 != null && Response.ok.equals(res4.getstatus())) {
                        String ans4 = ans ? " is no longer an Admin." : " is now an Admin.";
                        showalert(Alert.AlertType.INFORMATION, "Success", res.getusername() + ans4);
                        loadusers();
                    } else {
                        showalert(Alert.AlertType.ERROR, "Failed", "Could not change role.");
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleapprove(ActionEvent event) {
        Item res = pendingtable.getSelectionModel().getSelectedItem();
        if (res == null) return;
        new Thread(() -> {
            Request res1 = new Request(Request.approveitem, res.getid());
            Response res2 = NetworkClient.getinstance().sendrequestandwait(res1);
            if (res2 != null && Response.ok.equals(res2.getstatus())) {
                javafx.application.Platform.runLater(() -> {
                    pendinglist.remove(res);
                    showalert(Alert.AlertType.INFORMATION, "Approved", "Item '" + res.getname() + "' is now live.");
                    loadstats();
                });
            }
        }).start();
    }

    @FXML
    private void handlereject(ActionEvent event) {
        Item res = pendingtable.getSelectionModel().getSelectedItem();
        if (res == null) return;
        new Thread(() -> {
            Request res1 = new Request(Request.rejectitem, res.getid());
            Response res2 = NetworkClient.getinstance().sendrequestandwait(res1);
            if (res2 != null && Response.ok.equals(res2.getstatus())) {
                javafx.application.Platform.runLater(() -> {
                    pendinglist.remove(res);
                    showalert(Alert.AlertType.INFORMATION, "Rejected", "Item '" + res.getname() + "' has been rejected.");
                    loadstats();
                });
            }
        }).start();
    }

    @FXML
    private void handlefilterchange(ActionEvent event) {
        String res = ratingfilter.getValue();
        if (res == null || res.equals("All")) {
            filtereduserlist.setPredicate(ans -> true);
        } else if (res.equals("Positive")) {
            filtereduserlist.setPredicate(ans -> ans.gettotalratings() > 0 && ans.getavgrating() > 3.0);
        } else if (res.equals("Neutral")) {
            filtereduserlist.setPredicate(ans -> ans.gettotalratings() > 0 && ans.getavgrating() > 2.0 && ans.getavgrating() <= 3.0);
        } else if (res.equals("Negative")) {
            filtereduserlist.setPredicate(ans -> ans.gettotalratings() > 0 && ans.getavgrating() <= 2.0);
        } else if (res.equals("No Rating")) {
            filtereduserlist.setPredicate(ans -> ans.gettotalratings() <= 0);
        }
    }

    @FXML
    private void handlerefreshpending(ActionEvent event) {
        loadpendingitems();
        loadusers();
        loadstats();
    }

    private void showalert(Alert.AlertType type, String title, String content) {
        Alert res = new Alert(type);
        res.setTitle(title);
        res.setHeaderText(null);
        res.setContentText(content);
        res.showAndWait();
    }
}