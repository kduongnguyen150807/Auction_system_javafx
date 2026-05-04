package com.auction.client.ui.History;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryController {
  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryController.class);
  private static final Object EMPTY_PLACEHOLDER = new Object();

  @FXML private FlowPane ongoingcontainer;
  @FXML private FlowPane upcomingcontainer;
  @FXML private FlowPane closedcontainer;
  @FXML private FlowPane pastcontainer;

  private final PaneCards ongoingModel = new PaneCards();
  private final PaneCards upcomingModel = new PaneCards();
  private final PaneCards closedModel = new PaneCards();
  private final PaneCards pastModel = new PaneCards();

  private static final class PaneCards {
    final Map<Integer, ItemCardController> cards = new HashMap<>();
    final Map<Integer, Node> roots = new HashMap<>();
  }

  @FXML
  public void initialize() {
    refreshHistory();
  }

  public void refreshHistory() {
    if (ClientSession.getCurrentUser() == null) return;
    int userId = ClientSession.getCurrentUser().getId();
    Thread fetchThread =
        new Thread(
            () -> {
              List<Item> ongoing = fetchItems(Request.GET_ONGOING_BIDS, userId);
              List<Item> upcoming = fetchItems(Request.GET_UPCOMING_BIDS, userId);
              List<Item> closed = fetchItems("getclosedbids", userId);
              List<Item> past = fetchItems("getpastbids", userId);
              Platform.runLater(
                  () -> {
                    if (ongoingcontainer != null) {
                      incrementalRender(
                          ongoingcontainer, ongoing, ongoingModel, this::timeCaptionOngoing);
                    }
                    if (upcomingcontainer != null) {
                      incrementalRender(
                          upcomingcontainer, upcoming, upcomingModel, this::timeCaptionScheduled);
                    }
                    if (closedcontainer != null) {
                      incrementalRender(
                          closedcontainer, closed, closedModel, this::timeCaptionScheduled);
                    }
                    if (pastcontainer != null) {
                      incrementalRender(pastcontainer, past, pastModel, this::timeCaptionScheduled);
                    }
                  });
            });
    fetchThread.setDaemon(true);
    fetchThread.start();
  }

  private String timeCaptionOngoing(Item item) {
    return buildHistoryCaption(item, true);
  }

  private String timeCaptionScheduled(Item item) {
    return buildHistoryCaption(item, false);
  }

  private String buildHistoryCaption(Item item, boolean isOngoing) {
    String timeLabel = formatTime(isOngoing ? item.getEndTime() : item.getStartTime());
    if (item.getWinnerUsername() != null && !item.getWinnerUsername().isEmpty()) {
      timeLabel = "Winner: " + item.getWinnerUsername();
    }
    return timeLabel;
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

  private void incrementalRender(
      FlowPane pane,
      List<Item> items,
      PaneCards model,
      Function<Item, String> captionFn) {
    if (pane == null) return;
    Map<Integer, ItemCardController> cardMap = model.cards;
    Map<Integer, Node> rootByItemId = model.roots;

    if (items == null || items.isEmpty()) {
      for (Node n : new ArrayList<>(rootByItemId.values())) {
        pane.getChildren().remove(n);
      }
      cardMap.clear();
      rootByItemId.clear();
      pane.getChildren().clear();
      Label empty = new Label("Empty");
      empty.getStyleClass().add("card-text");
      empty.setUserData(EMPTY_PLACEHOLDER);
      pane.getChildren().add(empty);
      return;
    }

    pane.getChildren().removeIf(n -> EMPTY_PLACEHOLDER.equals(n.getUserData()));

    List<Item> ordered = new ArrayList<>(items);
    Set<Integer> desiredIds = new HashSet<>(ordered.size() * 2);
    for (Item it : ordered) desiredIds.add(it.getId());

    for (int id : new ArrayList<>(cardMap.keySet())) {
      if (!desiredIds.contains(id)) {
        cardMap.remove(id);
        Node removed = rootByItemId.remove(id);
        if (removed != null) pane.getChildren().remove(removed);
      }
    }

    for (Item item : ordered) {
      ItemCardController card = cardMap.get(item.getId());
      String caption = captionFn.apply(item);
      if (card != null) {
        card.syncFromCatalogItemStaticTime(item, caption);
      } else {
        try {
          NodeContentLoader<VBox> cardLoader = new NodeContentLoader<>();
          cardLoader.load("/fxml/itemcard/ItemCard.fxml");
          ItemCardController newCard = cardLoader.getController();
          VBox root = cardLoader.getCurrentNode();
          if (newCard != null && root != null) {
            newCard.setData(
                item.getId(),
                safe(item.getName()),
                item.getCurrentPrice(),
                safe(item.getDescription()),
                caption,
                safe(item.getImageUrl()),
                safe(item.getSellerUsername()),
                safe(item.getSellerAvatarUrl()));
            newCard.setEndTime(null);
            cardMap.put(item.getId(), newCard);
            rootByItemId.put(item.getId(), root);
          }
        } catch (Exception e) {
          LOGGER.warn("Failed to render history card for item id={}", item.getId(), e);
        }
      }
    }

    List<Node> orderedNodes = new ArrayList<>(ordered.size());
    for (Item item : ordered) {
      Node n = rootByItemId.get(item.getId());
      if (n != null) orderedNodes.add(n);
    }
    ObservableList<Node> children = pane.getChildren();
    boolean sameOrder = children.size() == orderedNodes.size();
    if (sameOrder) {
      for (int i = 0; i < orderedNodes.size(); i++) {
        if (children.get(i) != orderedNodes.get(i)) {
          sameOrder = false;
          break;
        }
      }
    }
    if (!sameOrder) children.setAll(orderedNodes);
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
