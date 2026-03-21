package com.auction.client.controller;
import com.auction.client.network.socketclient;
import com.auction.shared.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;
public class MenuController {
    @FXML private Button Login;
    @FXML private ScrollPane OngoingBind;
    @FXML private ScrollPane UpcomingBind;
    @FXML private ScrollPane EndedBind;
    public void initialize() {
        loaditems();
    }
    @FXML
    private void handlelogin() {
        System.out.println("chuan bi hien thi form dang nhap...");
    }
    private void loaditems() {
        Request req = new Request(1, "list_items", null);
        socketclient.getinstance().send(req);
        Response res = socketclient.getinstance().receive();
        if (res != null && res.getstatus().equals("ok")) {
            List<Item> items = (List<Item>) res.getdata();
            VBox ongoingbox = new VBox(10);
            VBox upcomingbox = new VBox(10);
            VBox endedbox = new VBox(10);
            for (Item i : items) {
                Label l = new Label(i.getname() + " - " + i.getcurrentprice() + "$");
                l.getStyleClass().add("item-label");
                long now = System.currentTimeMillis();
                if (java.sql.Timestamp.valueOf(i.getendtime()).getTime() < now) {
                    endedbox.getChildren().add(l);
                } else if (java.sql.Timestamp.valueOf(i.getstarttime()).getTime() > now) {
                    upcomingbox.getChildren().add(l);
                } else {
                    ongoingbox.getChildren().add(l);
                }
            }
            OngoingBind.setContent(ongoingbox);
            UpcomingBind.setContent(upcomingbox);
            EndedBind.setContent(endedbox);
        }
    }
}