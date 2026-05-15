package com.auction.client.ui.History;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class HistoryController {
  private static final Object emptyplaceholder = new Object();
  @FXML private FlowPane ongoingcontainer;
  @FXML private FlowPane upcomingcontainer;
  @FXML private FlowPane closedcontainer;
  @FXML private FlowPane pastcontainer;
  @FXML private FlowPane watchlistcontainer;
  private final PaneCards watchlistmodel = new PaneCards();
  private final PaneCards trendingSectionCards = new PaneCards();
  private final PaneCards upcomingmodel = new PaneCards();
  private final PaneCards closedmodel = new PaneCards();
  private final PaneCards pastmodel = new PaneCards();
  private final AtomicLong fetchgen = new AtomicLong(0);

  private static final class PaneCards {
    final Map<Integer, ItemCardController> cards = new HashMap<>();
    final Map<Integer, Node> roots = new HashMap<>();
  }

  @FXML
  public void initialize() {
    refreshHistory();
  }

  public void refreshHistory() {
    if (ClientSession.getCurrentUser() == null) {
      return;
    }
    int userid = ClientSession.getCurrentUser().getId();
    long currentgen = fetchgen.incrementAndGet();
    Thread thread = new Thread(() -> {
      List<Item> watchlist = fetchitems(Request.GET_WATCHLIST_ITEMS, userid); // Lấy Watchlist
      List<Item> trending = fetchTrendingLotsForCatalogType();
      List<Item> upcoming = fetchitems(Request.GET_UPCOMING_BIDS, userid);
      List<Item> closed = fetchitems("getclosedbids", userid);
      List<Item> past = fetchitems("getpastbids", userid);

      Platform.runLater(() -> {
        if (currentgen != fetchgen.get()) return;

        // Render Watchlist
        if (watchlistcontainer != null && watchlist != null) {
          incrementalrender(watchlistcontainer, watchlist, watchlistmodel, this::timecaptionscheduled);
        }

        if (ongoingcontainer != null && trending != null) {
          incrementalrender(ongoingcontainer, trending, trendingSectionCards, this::timecaptionongoing);
        }
        if (upcomingcontainer != null && upcoming != null) {
          incrementalrender(upcomingcontainer, upcoming, upcomingmodel, this::timecaptionscheduled);
        }
        if (closedcontainer != null && closed != null) {
          incrementalrender(closedcontainer, closed, closedmodel, this::timecaptionscheduled);
        }
        if (pastcontainer != null && past != null) {
          incrementalrender(pastcontainer, past, pastmodel, this::timecaptionscheduled);
        }
      });
    });
    thread.setDaemon(true);
    thread.start();
  }

  private String timecaptionongoing(Item item) {
    String ans = buildhistorycaption(item, true);
    return ans;
  }

  private String timecaptionscheduled(Item item) {
    String ans = buildhistorycaption(item, false);
    return ans;
  }

  private String buildhistorycaption(Item item, boolean isongoing) {
    String timelabel = formattime(isongoing ? item.getEndTime() : item.getStartTime());
    if (item.getWinnerUsername() != null && !item.getWinnerUsername().isEmpty()) {
      timelabel = "Winner: " + item.getWinnerUsername();
    }
    return timelabel;
  }

  /**
   * Cùng nguồn với hàng Trending trên Trang chủ — theo kiểu English / Dutch đang chọn trên sidebar.
   */
  @SuppressWarnings("unchecked")
  private List<Item> fetchTrendingLotsForCatalogType() {
    AuctionType kind = KhungController.getCatalogAuctionType();
    if (kind == null) {
      kind = AuctionType.ENGLISH;
    }
    Request req = new Request(Request.GET_TRENDING_LOTS, kind.dbName());
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && Response.OK.equals(res.getStatus())) {
      Object payload = res.getPayload();
      if (payload instanceof List) {
        return (List<Item>) payload;
      }
    }
    return new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  private List<Item> fetchitems(String action, int userid) {
    Request req = new Request(action, userid);
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && Response.OK.equals(res.getStatus())) {
      Object payload = res.getPayload();
      if (payload instanceof List) {
        List<Item> ans = (List<Item>) payload;
        return ans;
      }
    }
    return null;
  }

  private void incrementalrender(FlowPane pane, List<Item> items, PaneCards model, Function<Item, String> captionfn) {
    if (pane == null) {
      return;
    }
    Map<Integer, ItemCardController> cardmap = model.cards;
    Map<Integer, Node> rootbyitemid = model.roots;
    if (items.isEmpty()) {
      for (Node n : new ArrayList<>(rootbyitemid.values())) {
        pane.getChildren().remove(n);
      }
      cardmap.clear();
      rootbyitemid.clear();
      pane.getChildren().clear();
      Label empty = new Label("Empty");
      empty.getStyleClass().add("card-text");
      empty.setUserData(emptyplaceholder);
      pane.getChildren().add(empty);
      return;
    }
    pane.getChildren().removeIf(n -> emptyplaceholder.equals(n.getUserData()));
    List<Item> ordered = new ArrayList<>(items);
    Set<Integer> desiredids = new HashSet<>(ordered.size() * 2);
    for (Item it : ordered) {
      desiredids.add(it.getId());
    }
    for (int id : new ArrayList<>(cardmap.keySet())) {
      if (!desiredids.contains(id)) {
        cardmap.remove(id);
        Node removed = rootbyitemid.remove(id);
        if (removed != null) {
          pane.getChildren().remove(removed);
        }
      }
    }
    for (Item item : ordered) {
      ItemCardController card = cardmap.get(item.getId());
      String caption = captionfn.apply(item);
      if (card != null) {
        card.syncFromCatalogItemStaticTime(item, caption);
        card.attachCatalogItem(item);
      } else {
        try {
          NodeContentLoader<VBox> cardloader = new NodeContentLoader<>();
          cardloader.load("/fxml/itemcard/ItemCard.fxml");
          ItemCardController newcard = cardloader.getController();
          VBox root = cardloader.getCurrentNode();
          if (newcard != null && root != null) {
            newcard.setData(item.getId(), safe(item.getName()), item.getCurrentPrice(), safe(item.getDescription()), caption, safe(item.getImageUrl()), safe(item.getSellerUsername()), safe(item.getSellerAvatarUrl()));
            newcard.setEndTime(null);
            newcard.attachCatalogItem(item);
            cardmap.put(item.getId(), newcard);
            rootbyitemid.put(item.getId(), root);
          }
        } catch (Exception e) {
        }
      }
    }
    List<Node> orderednodes = new ArrayList<>(ordered.size());
    for (Item item : ordered) {
      Node n = rootbyitemid.get(item.getId());
      if (n != null) {
        orderednodes.add(n);
      }
    }
    ObservableList<Node> children = pane.getChildren();
    boolean sameorder = children.size() == orderednodes.size();
    if (sameorder) {
      for (int i = 0; i < orderednodes.size(); i++) {
        if (children.get(i) != orderednodes.get(i)) {
          sameorder = false;
          break;
        }
      }
    }
    if (!sameorder) {
      children.setAll(orderednodes);
    }
  }

  private String safe(String value) {
    String ans = value == null ? "" : value;
    return ans;
  }

  private String formattime(LocalDateTime time) {
    if (time == null) {
      return "N/A";
    }
    Duration remaining = Duration.between(LocalDateTime.now(), time);
    if (remaining.isNegative() || remaining.isZero()) {
      return "closed";
    }
    long hours = remaining.toHours();
    if (hours / 24 > 0) {
      String ans = (hours / 24) + "d " + (hours % 24) + "h";
      return ans;
    }
    String ans = (hours % 24) + "h " + (remaining.toMinutes() % 60) + "m";
    return ans;
  }
  public void updateWatchlistUi(int itemId, boolean isWatched) {
    updateCardHeart(trendingSectionCards, itemId, isWatched);
    updateCardHeart(upcomingmodel, itemId, isWatched);
    updateCardHeart(closedmodel, itemId, isWatched);
    updateCardHeart(pastmodel, itemId, isWatched);
    // Riêng thẻ trong mục Watchlist, nếu bỏ tim thì ta vẫn giữ nguyên trên màn hình (chỉ đổi icon thành trắng)
    // để tránh việc UI bị giật/biến mất đột ngột. Lần sau user ấn Refresh nó mới biến mất.
    updateCardHeart(watchlistmodel, itemId, isWatched);
  }

  private void updateCardHeart(PaneCards model, int itemId, boolean isWatched) {
    ItemCardController card = model.cards.get(itemId);
    if (card != null) card.setHeartUI(isWatched);
  }
}