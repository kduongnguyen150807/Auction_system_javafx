package com.auction.client.ui.History;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.Lot;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class HistoryController {
  @FXML private FlowPane ongoingcontainer;
  @FXML private FlowPane upcomingcontainer;
  @FXML private FlowPane closedcontainer;
  @FXML private FlowPane pastcontainer;

  @FXML
  public void initialize() {
    refreshHistory();
  }

  public void refreshHistory() {
    if (ClientSession.getCurrentUser() == null) return;
    int res = ClientSession.getCurrentUser().getId();
    Thread t =
        new Thread(
            () -> {
              List<Lot> ans1 = fetchOngoing(res);
              List<Lot> ans2 = fetchUpcoming(res);
              List<Lot> ans3 = fetchClosed(res);
              List<Lot> ans4 = fetchPast(res);
              Platform.runLater(
                  () -> {
                    if (ongoingcontainer != null) renderCards(ongoingcontainer, ans1, true);
                    if (upcomingcontainer != null) renderCards(upcomingcontainer, ans2, false);
                    if (closedcontainer != null) renderCards(closedcontainer, ans3, false);
                    if (pastcontainer != null) renderCards(pastcontainer, ans4, false);
                  });
            });
    t.setDaemon(true);
    t.start();
  }

  @SuppressWarnings("unchecked")
  private List<Lot> fetchOngoing(int id) {
    Request req = new Request(Request.GET_ONGOING_BIDS, id);
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && Response.OK.equals(res.getStatus())) {
      Object ans = res.getPayload();
      if (ans instanceof List) return (List<Lot>) ans;
    }
    return java.util.Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  private List<Lot> fetchUpcoming(int id) {
    Request req = new Request(Request.GET_UPCOMING_BIDS, id);
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && Response.OK.equals(res.getStatus())) {
      Object ans = res.getPayload();
      if (ans instanceof List) return (List<Lot>) ans;
    }
    return java.util.Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  private List<Lot> fetchClosed(int id) {
    Request req = new Request("getclosedbids", id);
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && Response.OK.equals(res.getStatus())) {
      Object ans = res.getPayload();
      if (ans instanceof List) return (List<Lot>) ans;
    }
    return java.util.Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  private List<Lot> fetchPast(int id) {
    Request req = new Request("getpastbids", id);
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && Response.OK.equals(res.getStatus())) {
      Object ans = res.getPayload();
      if (ans instanceof List) return (List<Lot>) ans;
    }
    return java.util.Collections.emptyList();
  }

  private void renderCards(FlowPane p, List<Lot> list, boolean isOngoing) {
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
          String time = formatTime(isOngoing ? res.getEndTime() : res.getStartTime());
          if (res.getWinnerUsername() != null && !res.getWinnerUsername().isEmpty()) {
            time = "Winner: " + res.getWinnerUsername();
          }
          ctrl.setData(
              res.getId(),
              safe(res.getTitle()),
              res.getBidValue(),
              safe(res.getDescription()),
              time,
              safe(res.getImageUrl()),
              safe(res.getSellerUsername()),
              safe(res.getSellerAvatarUrl()));
        }
        NodeManager.addNodeToPane(l, p);
      } catch (Exception e) {
      }
    }
  }

  private String safe(String s) {
    return (s == null) ? "" : s;
  }

  private String formatTime(LocalDateTime t) {
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
