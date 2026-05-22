package com.auction.client.ui.Live;

import com.auction.client.ClientSession;
import com.auction.client.live.UdpRelayVideoTransport;
import com.auction.client.network.NetworkClient;
import com.auction.client.service.LiveAuctionClientService;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.LiveBidTiers;
import com.auction.shared.LiveParticipantEvent;
import com.auction.shared.LiveParticipantSummary;
import com.auction.shared.LiveSessionInfo;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveAuctionController {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiveAuctionController.class);
  private static LiveAuctionController instance;

  @FXML private ListView<Item> liveItemList;
  @FXML private Button joinButton;
  @FXML private Button leaveButton;
  @FXML private Button refreshButton;
  @FXML private Label sessionStatusLabel;
  @FXML private Label itemTitleLabel;
  @FXML private Label currentPriceLabel;
  @FXML private Label bidStatusLabel;
  @FXML private FlowPane videoGrid;
  @FXML private Button tier1Button;
  @FXML private Button tier2Button;
  @FXML private Button tier3Button;
  @FXML private TextField customBidField;
  @FXML private Button placeBidButton;

  private final LiveAuctionClientService liveService = new LiveAuctionClientService();
  private final UdpRelayVideoTransport videoTransport = new UdpRelayVideoTransport();
  private final Map<Integer, VBox> participantTiles = new HashMap<>();
  private int localUserId = -1;

  private int activeItemId = -1;
  private double activeCurrentPrice;
  private LiveBidTiers activeTiers;

  public static LiveAuctionController getInstance() {
    return instance;
  }

  @FXML
  public void initialize() {
    instance = this;
    liveItemList.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          setText("#" + item.getId() + " — " + item.getName());
        }
      }
    });
    videoTransport.setFrameListener(this::onVideoFrame);
    videoTransport.setStatusListener(msg -> Platform.runLater(() -> {
      if (sessionStatusLabel != null && activeItemId > 0) {
        sessionStatusLabel.setText(msg);
      }
    }));
    refreshLiveList();
  }

  public void refreshOnNavigate() {
    refreshLiveList();
  }

  /** Navigate from item detail — load list, select lot, join session. */
  public void openForItem(int itemId) {
    if (itemId <= 0) {
      return;
    }
    User me = ClientSession.getCurrentUser();
    if (me == null) {
      showStatus("Vui lòng đăng nhập.");
      return;
    }
    new Thread(() -> {
      List<Item> items = liveService.fetchLiveAuctions();
      Item target =
          items.stream().filter(i -> i.getId() == itemId).findFirst().orElse(null);
      if (target == null) {
        target = fetchItemFallback(itemId);
      }
      Item resolved = target;
      Platform.runLater(() -> {
        liveItemList.getItems().setAll(items);
        if (resolved == null) {
          showStatus("Lot LIVE #" + itemId + " không khả dụng.");
          return;
        }
        liveItemList.getSelectionModel().select(resolved);
        if (!liveItemList.getItems().contains(resolved)) {
          liveItemList.getItems().add(resolved);
          liveItemList.getSelectionModel().select(resolved);
        }
        joinItem(resolved, me);
      });
    }).start();
  }

  private Item fetchItemFallback(int itemId) {
    try {
      com.auction.client.service.BiddingClientService bidding = new com.auction.client.service.BiddingClientService();
      Item item = bidding.getItemById(itemId);
      if (item != null && item.getAuctionType() == com.auction.shared.AuctionType.LIVE) {
        return item;
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  private void joinItem(Item item, User me) {
    new Thread(() -> {
      LiveSessionInfo info = liveService.joinSession(item.getId());
      Platform.runLater(() -> {
        if (info == null) {
          showStatus("Không tham gia được phiên live.");
          return;
        }
        enterSession(item, info, me.getId());
      });
    }).start();
  }

  @FXML
  private void handleRefreshList() {
    refreshLiveList();
  }

  @FXML
  private void handleJoin() {
    Item selected = liveItemList.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showStatus("Chọn một lot LIVE trước.");
      return;
    }
    User me = ClientSession.getCurrentUser();
    if (me == null) {
      showStatus("Vui lòng đăng nhập.");
      return;
    }
    new Thread(() -> {
      LiveSessionInfo info = liveService.joinSession(selected.getId());
      Platform.runLater(() -> {
        if (info == null) {
          showStatus("Không tham gia được phiên live.");
          return;
        }
        enterSession(selected, info, me.getId());
      });
    }).start();
  }

  @FXML
  private void handleLeave() {
    leaveSession();
  }

  @FXML
  private void handleTier1() {
    bidAt(activeTiers != null ? activeTiers.getTier1() : 0);
  }

  @FXML
  private void handleTier2() {
    bidAt(activeTiers != null ? activeTiers.getTier2() : 0);
  }

  @FXML
  private void handleTier3() {
    bidAt(activeTiers != null ? activeTiers.getTier3() : 0);
  }

  @FXML
  private void handlePlaceBid() {
    try {
      double value = Double.parseDouble(customBidField.getText().trim());
      bidAt(value);
    } catch (NumberFormatException e) {
      bidStatusLabel.setText("Giá không hợp lệ.");
    }
  }

  public void onLiveParticipantUpdate(LiveParticipantEvent event) {
    if (event == null || event.getItemId() != activeItemId) {
      return;
    }
    Platform.runLater(() -> {
      if (event.getAction() == LiveParticipantEvent.Action.JOINED) {
        ensureParticipantTile(event.getUserId(), event.getUsername());
      } else {
        removeParticipantTile(event.getUserId());
      }
      sessionStatusLabel.setText(participantTiles.size() + " người trong phiên");
    });
  }

  public void onPriceUpdate(Item item) {
    if (item == null || item.getId() != activeItemId) {
      return;
    }
    Platform.runLater(() -> applyPrice(item.getCurrentPrice()));
  }

  private void enterSession(Item item, LiveSessionInfo info, int userId) {
    activeItemId = item.getId();
    localUserId = userId;
    activeCurrentPrice = item.getCurrentPrice();
    itemTitleLabel.setText(item.getName());
    applyPrice(activeCurrentPrice);
    videoGrid.getChildren().clear();
    participantTiles.clear();
    for (LiveParticipantSummary p : info.getParticipants()) {
      ensureParticipantTile(p.getUserId(), p.getUsername());
    }
    joinButton.setDisable(true);
    leaveButton.setDisable(false);
    tier1Button.setDisable(false);
    tier2Button.setDisable(false);
    tier3Button.setDisable(false);
    customBidField.setDisable(false);
    placeBidButton.setDisable(false);
    sessionStatusLabel.setText(info.getParticipantCount() + " người trong phiên — UDP :" + info.getUdpPort());

    String host = NetworkClient.getInstance().getServerHost();
    videoTransport.start(info.getItemId(), userId, host, info.getUdpPort());
    refreshTiers();
  }

  private void leaveSession() {
    videoTransport.stop();
    liveService.leaveSession();
    activeItemId = -1;
    localUserId = -1;
    activeTiers = null;
    videoGrid.getChildren().clear();
    participantTiles.clear();
    joinButton.setDisable(false);
    leaveButton.setDisable(true);
    tier1Button.setDisable(true);
    tier2Button.setDisable(true);
    tier3Button.setDisable(true);
    customBidField.setDisable(true);
    placeBidFieldClear();
    placeBidButton.setDisable(true);
    itemTitleLabel.setText("Chưa chọn phiên");
    currentPriceLabel.setText("Giá hiện tại: —");
    sessionStatusLabel.setText("Đã rời phiên live");
    bidStatusLabel.setText("");
  }

  private void placeBidFieldClear() {
    customBidField.clear();
  }

  private void refreshLiveList() {
    new Thread(() -> {
      List<Item> items = liveService.fetchLiveAuctions();
      Platform.runLater(() -> liveItemList.getItems().setAll(items));
    }).start();
  }

  private void refreshTiers() {
    if (activeItemId <= 0) {
      return;
    }
    new Thread(() -> {
      LiveBidTiers tiers = liveService.fetchBidTiers(activeItemId);
      Platform.runLater(() -> {
        activeTiers = tiers;
        if (tiers != null) {
          tier1Button.setText(formatTier("Tier 1", tiers.getTier1()));
          tier2Button.setText(formatTier("Tier 2", tiers.getTier2()));
          tier3Button.setText(formatTier("Tier 3", tiers.getTier3()));
        }
      });
    }).start();
  }

  private void bidAt(double amount) {
    if (activeItemId <= 0 || amount <= activeCurrentPrice) {
      bidStatusLabel.setText("Giá phải cao hơn giá hiện tại.");
      return;
    }
    User me = ClientSession.getCurrentUser();
    if (me == null) {
      return;
    }
    BidTransaction bid = new BidTransaction();
    bid.setItemId(activeItemId);
    bid.setUserId(me.getId());
    bid.setBidValue(amount);
    bid.setAutoBid(false);
    new Thread(() -> {
      Response res = liveService.placeBid(bid);
      Platform.runLater(() -> {
        if (res != null && Response.OK.equals(res.getStatus())) {
          bidStatusLabel.setText("Đặt giá thành công: " + String.format("%,.0f$", amount));
          refreshTiers();
        } else {
          bidStatusLabel.setText(res != null ? res.getMessage() : "Đặt giá thất bại");
        }
      });
    }).start();
  }

  private void applyPrice(double price) {
    activeCurrentPrice = price;
    currentPriceLabel.setText("Giá hiện tại: " + String.format("%,.0f$", price));
    refreshTiers();
  }

  private void onVideoFrame(int userId, byte[] jpeg) {
    Platform.runLater(() -> {
      String label =
          userId == localUserId
              ? resolveUsername(userId) + " (Bạn)"
              : resolveUsername(userId);
      VBox tile = participantTiles.get(userId);
      if (tile == null) {
        tile = ensureParticipantTile(userId, label);
      }
      ImageView view = (ImageView) tile.getUserData();
      if (view != null) {
        try {
          view.setImage(new Image(new ByteArrayInputStream(jpeg), 200, 150, true, true));
        } catch (Exception e) {
          LOGGER.debug("Failed to decode frame for user {}", userId, e);
        }
      }
    });
  }

  private String resolveUsername(int userId) {
    User me = ClientSession.getCurrentUser();
    if (me != null && me.getId() == userId) {
      return me.getUsername() != null ? me.getUsername() : ("User " + userId);
    }
    return "User " + userId;
  }

  private VBox ensureParticipantTile(int userId, String username) {
    VBox existing = participantTiles.get(userId);
    if (existing != null) {
      return existing;
    }
    ImageView imageView = new ImageView();
    imageView.setFitWidth(200);
    imageView.setFitHeight(150);
    imageView.setPreserveRatio(true);
    imageView.getStyleClass().add("live-video-frame");
    Label name = new Label(username != null ? username : ("User " + userId));
    name.getStyleClass().add("participant-name");
    VBox box = new VBox(8, imageView, name);
    box.getStyleClass().add("live-participant-tile");
    box.setUserData(imageView);
    participantTiles.put(userId, box);
    videoGrid.getChildren().add(box);
    return box;
  }

  private void removeParticipantTile(int userId) {
    VBox box = participantTiles.remove(userId);
    if (box != null) {
      videoGrid.getChildren().remove(box);
    }
  }

  private static String formatTier(String label, double value) {
    return label + ": " + String.format("%,.0f$", value);
  }

  private void showStatus(String msg) {
    sessionStatusLabel.setText(msg);
  }
}
