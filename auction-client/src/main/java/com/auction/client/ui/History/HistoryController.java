package com.auction.client.ui.History;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.Lot;
import com.auction.shared.Request;
import com.auction.shared.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class HistoryController {
    @FXML private FlowPane ongoingcontainer;
    @FXML private FlowPane upcomingcontainer;
    @FXML private FlowPane closedcontainer;
    @FXML private FlowPane pastcontainer;

    @FXML
    public void initialize() {
        refreshhistory();
    }

    public void refreshhistory() {
        if (ClientSession.getCurrentUser() == null) return;
        int res = ClientSession.getCurrentUser().getid();
        Thread t = new Thread(() -> {
            List<Lot> ans1 = fetchongoing(res);
            List<Lot> ans2 = fetchupcoming(res);
            List<Lot> ans3 = fetchclosed(res);
            List<Lot> ans4 = fetchpast(res);
            Platform.runLater(() -> {
                if (ongoingcontainer != null) rendercards(ongoingcontainer, ans1, true);
                if (upcomingcontainer != null) rendercards(upcomingcontainer, ans2, false);
                if (closedcontainer != null) rendercards(closedcontainer, ans3, false);
                if (pastcontainer != null) rendercards(pastcontainer, ans4, false);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    @SuppressWarnings("unchecked")
    private List<Lot> fetchongoing(int id) {
        Request req = new Request(Request.getongoingbids, id);
        Response res = NetworkClient.getinstance().sendrequestandwait(req);
        if (res != null && Response.ok.equals(res.getstatus())) {
            Object ans = res.getpayload();
            if (ans instanceof List) return (List<Lot>) ans;
        }
        return java.util.Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Lot> fetchupcoming(int id) {
        Request req = new Request(Request.getupcomingbids, id);
        Response res = NetworkClient.getinstance().sendrequestandwait(req);
        if (res != null && Response.ok.equals(res.getstatus())) {
            Object ans = res.getpayload();
            if (ans instanceof List) return (List<Lot>) ans;
        }
        return java.util.Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Lot> fetchclosed(int id) {
        Request req = new Request("getclosedbids", id);
        Response res = NetworkClient.getinstance().sendrequestandwait(req);
        if (res != null && Response.ok.equals(res.getstatus())) {
            Object ans = res.getpayload();
            if (ans instanceof List) return (List<Lot>) ans;
        }
        return java.util.Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Lot> fetchpast(int id) {
        Request req = new Request("getpastbids", id);
        Response res = NetworkClient.getinstance().sendrequestandwait(req);
        if (res != null && Response.ok.equals(res.getstatus())) {
            Object ans = res.getpayload();
            if (ans instanceof List) return (List<Lot>) ans;
        }
        return java.util.Collections.emptyList();
    }

    private void rendercards(FlowPane p, List<Lot> list, boolean isongoing) {
        p.getChildren().clear();
        if (list == null || list.isEmpty()) {
            Label ans = new Label("Trống");
            ans.getStyleClass().add("card-text");
            p.getChildren().add(ans);
            return;
        }
        for (Lot res : list) {
            try {
                NodeContentLoader<HBox> l = new NodeContentLoader<>();
                l.load("/fxml/itemcard/ItemCard.fxml");
                ItemCardController ctrl = l.getController();
                if (ctrl != null) {
                    String time = formattime(isongoing ? res.getendtime() : res.getstarttime());
                    if (res.getwinnerusername() != null && !res.getwinnerusername().isEmpty()) {
                        time = "Winner: " + res.getwinnerusername();
                    }
                    ctrl.setData(res.getid(), safe(res.gettitle()), res.getbidvalue(), safe(res.getdescription()), time, safe(res.getimageurl()), safe(res.getsellerusername()), safe(res.getselleravatarurl()));
                }
                NodeManager.addNodeToPane(l, p);
            } catch (Exception e) {}
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private String formattime(LocalDateTime t) {
        if (t == null) return "N/A";
        Duration d = Duration.between(LocalDateTime.now(), t);
        if (d.isNegative() || d.isZero()) return "closed";
        long h = d.toHours();
        long day = h / 24;
        long hour = h % 24;
        if (day > 0) return day + "d " + hour + "h";
        long min = d.toMinutes() % 60;
        return hour + "h " + min + "m";
    }
}