package com.auction.client.ui.History;
import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.shared.Lot;
import com.auction.shared.Request;
import com.auction.shared.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;
public class HistoryController {
    @FXML private FlowPane ongoingcontainer;
    @FXML private FlowPane upcomingcontainer;
    @FXML
    public void initialize() {
        refreshhistory();
    }
    public void refreshhistory() {
        if (ClientSession.getCurrentUser() == null) return;
        int res = ClientSession.getCurrentUser().getid();
        new Thread(() -> {
            List<Lot> ans = fetchongoing(res);
            List<Lot> ans2 = fetchupcoming(res);
            Platform.runLater(() -> {
                rendercards(ongoingcontainer, ans, true);
                rendercards(upcomingcontainer, ans2, false);
            });
        }).start();
    }
    @SuppressWarnings("unchecked")
    private List<Lot> fetchongoing(int userid) {
        Request req = new Request(Request.getongoingbids, userid);
        Response res = NetworkClient.getinstance().sendrequestandwait(req);
        if (res != null && Response.ok.equals(res.getstatus())) {
            Object ans = res.getpayload();
            if (ans instanceof List) {
                List<Lot> res2 = (List<Lot>) ans;
                return res2;
            }
        }
        return java.util.Collections.emptyList();
    }
    @SuppressWarnings("unchecked")
    private List<Lot> fetchupcoming(int userid) {
        Request req = new Request(Request.getupcomingbids, userid);
        Response res = NetworkClient.getinstance().sendrequestandwait(req);
        if (res != null && Response.ok.equals(res.getstatus())) {
            Object ans = res.getpayload();
            if (ans instanceof List) {
                List<Lot> res2 = (List<Lot>) ans;
                return res2;
            }
        }
        return java.util.Collections.emptyList();
    }
    private void rendercards(FlowPane container, List<Lot> lots, boolean isongoing) {
        container.getChildren().clear();
        if (lots == null || lots.isEmpty()) {
            Label ans = new Label(isongoing ? "No ongoing bids" : "No upcoming bids");
            ans.getStyleClass().add("card-text");
            container.getChildren().add(ans);
            return;
        }
        for (Lot res : lots) {
            VBox ans = new VBox(6);
            ans.getStyleClass().add("history-card");
            Label res2 = new Label(res.gettitle() != null ? res.gettitle() : "Untitled");
            res2.getStyleClass().add("card-text");
            Label res3 = new Label(String.format("Current: %,.2f$", res.getbidvalue()));
            res3.getStyleClass().add("history-subtitle");
            String res4 = isongoing ? "Ends: " + formattime(res.getendtime()) : "Starts: " + formattime(res.getstarttime());
            Label res5 = new Label(res4);
            res5.getStyleClass().add("history-subtitle");
            ans.getChildren().addAll(res2, res3, res5);
            container.getChildren().add(ans);
        }
    }
    private String formattime(java.time.LocalDateTime res) {
        if (res == null) {
            String ans = "N/A";
            return ans;
        }
        String ans = res.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        return ans;
    }
}
