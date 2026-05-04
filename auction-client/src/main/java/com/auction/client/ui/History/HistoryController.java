package com.auction.client.ui.History;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryController {
  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryController.class);

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
    int userId = ClientSession.getCurrentUser().getId();
    Thread fetchThread = new Thread(() -> {
      List<Item> ongoing = fetchItems(Request.GET_ONGOING_BIDS, userId);
      List<Item> upcoming = fetchItems(Request.GET_UPCOMING_BIDS, userId);
      List<Item> closed = fetchItems("getclosedbids", userId);
      List<Item> past = fetchItems("getpastbids", userId);
      Platform.runLater(() -> {
        if (ongoingcontainer != null) renderCards(ongoingcontainer, ongoing, true);
        if (upcomingcontainer != null) renderCards(upcomingcontainer, upcoming, false);
        if (closedcontainer != null) renderCards(closedcontainer, closed, false);
        if (pastcontainer != null) renderCards(pastcontainer, past, false);
      });
    });
    fetchThread.setDaemon(true);
    fetchThread.start();
  }

  @SuppressWarnings("unchecked")
  private List<Item> fetchItems(String action, int userId) {
    Request request = new Request(action, userId);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      Object payload = response.getPayload();
      if (payload instanceof List) return (List<Item>) payload;
    }
    return Collections.emptyList();
  }

  private void renderCards(FlowPane pane, List<Item> items, boolean isOngoing) {
    pane.getChildren().clear();
    if (items == null || items.isEmpty()) {
      Label empty = new Label("Empty");
      empty.getStyleClass().add("card-text");
      pane.getChildren().add(empty);
      return;
    }
    for (Item item : items) {
      try {
        NodeContentLoader<HBox> cardLoader = new NodeContentLoader<>();
        cardLoader.load("/fxml/itemcard/ItemCard.fxml");
        ItemCardController card = cardLoader.getController();
        if (card != null) {
          String timeLabel = formatTime(isOngoing ? item.getEndTime() : item.getStartTime());
          if (item.getWinnerUsername() != null && !item.getWinnerUsername().isEmpty()) {
            timeLabel = "Winner: " + item.getWinnerUsername();
          }
          card.setData(
              item.getId(),
              safe(item.getName()),
              item.getCurrentPrice(),
              safe(item.getDescription()),
              timeLabel,
              safe(item.getImageUrl()),
              safe(item.getSellerUsername()),
              safe(item.getSellerAvatarUrl()));
        }
        NodeManager.addNodeToPane(cardLoader, pane);
      } catch (Exception e) {
        LOGGER.warn("Failed to render history card for item id={}", item.getId(), e);
      }
    }
  }

  private String safe(String value) {
    return (value == null) ? "" : value;
  }

  private String formatTime(LocalDateTime time) {
    if (time == null) return "N/A";
    Duration remaining = Duration.between(LocalDateTime.now(), time);
    if (remaining.isNegative() || remaining.isZero()) return "closed";
    long hours = remaining.toHours();
    if (hours / 24 > 0) return (hours / 24) + "d " + (hours % 24) + "h";
    return (hours % 24) + "h " + (remaining.toMinutes() % 60) + "m";
  }
}
