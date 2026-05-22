package com.auction.client.ui.Main;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkEventListener;
import com.auction.client.ui.Chat.ChatPageController;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import com.auction.client.ui.Live.LiveAuctionController;
import com.auction.client.ui.TrangChu.TrangChuController;
import com.auction.client.util.ItemNotificationText;
import com.auction.client.util.NotificationCenter;
import com.auction.shared.ChatMessage;
import com.auction.shared.Friendship;
import com.auction.shared.Item;
import com.auction.shared.LiveParticipantEvent;
import com.auction.shared.User;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/** Dispatches server push events into shell controllers and notifications. */
final class MainShellNetworkBridge implements NetworkEventListener {

  private final Supplier<TrangChuController> homeController;
  private final Supplier<ChatPageController> chatController;

  MainShellNetworkBridge(
      Supplier<TrangChuController> homeController, Supplier<ChatPageController> chatController) {
    this.homeController = homeController;
    this.chatController = chatController;
  }

  private TrangChuController home() {
    return homeController.get();
  }

  private ChatPageController chat() {
    return chatController.get();
  }

  private static ItemInformationController detail() {
    return KhungController.itemDetailController;
  }
  @Override
  public void onLeaderboardUpdate(java.util.List<com.auction.shared.LeaderboardEntry> res) {
    TrangChuController ans = home();
    if (ans != null) {
      ans.updateleaderboardui(res);
    }
  }
  @Override
  public void onNewBidUpdate(Item item) {
    ItemInformationController d = detail();
    if (d != null) d.updatePriceUi(item);
    TrangChuController h = home();
    if (h != null) h.updatePriceUi(item);
    LiveAuctionController live = LiveAuctionController.getInstance();
    if (live != null) {
      live.onPriceUpdate(item);
    }
  }

  @Override
  public void onLiveParticipantUpdate(LiveParticipantEvent event) {
    LiveAuctionController live = LiveAuctionController.getInstance();
    if (live != null) {
      live.onLiveParticipantUpdate(event);
    }
  }

  @Override
  public void onItemClosed(Item item) {
    TrangChuController h = home();
    if (h != null) h.removeClosedItem(item);
    ItemInformationController d = detail();
    if (d != null) d.markItemClosed(item);
    User me = ClientSession.getCurrentUser();
    if (me != null && item.getSellerId() == me.getId())
      NotificationCenter.addNotification(ItemNotificationText.itemClosed(item));
  }

  @Override
  public void onSellerBidNotify(Item item, double newBidPrice) {
    NotificationCenter.addNotification(ItemNotificationText.sellerNewBid(item, newBidPrice));
  }

  @Override
  public void onOutbidNotify(Item item) {
    NotificationCenter.addNotification(ItemNotificationText.outbid(item));
  }

  @Override
  public void onGlobalChat(ChatMessage message) {
    ChatPageController c = chat();
    if (c != null) c.onGlobalChat(message);
  }

  @Override
  public void onPrivateChat(ChatMessage message) {
    ChatPageController c = chat();
    if (c != null) c.onPrivateChat(message);
  }

  @Override
  public void onFriendRequest(Friendship friendship) {
    NotificationCenter.addNotification(friendship.getRequesterUsername() + " đã gửi lời mời kết bạn!");
    ChatPageController c = chat();
    if (c != null) c.onFriendRequest(friendship);
  }

  @Override
  public void onFriendRequestSent(Friendship friendship) {
    NotificationCenter.addNotification("Đã gửi lời mời kết bạn đến " + friendship.getAddresseeUsername() + "!");
    ChatPageController c = chat();
    if (c != null) c.onFriendRequestSent(friendship);
  }

  @Override
  public void onFriendAccepted(Friendship friendship) {
    ChatPageController c = chat();
    if (c != null) c.onFriendAccepted(friendship);
    User me = ClientSession.getCurrentUser();
    NotificationCenter.addNotification(
        me != null && friendship.getRequesterId() == me.getId()
            ? friendship.getAddresseeUsername() + " đã chấp nhận lời mời kết bạn!"
            : "Bạn và " + friendship.getRequesterUsername() + " đã trở thành bạn bè!");
  }

  @Override
  public void onAccountBanned(String reason) {
    showFxAlert(
        Alert.AlertType.ERROR,
        "Account Suspended",
        "Your account has been suspended by an administrator.",
        reason != null && !reason.isBlank() ? reason : "You have been banned. Please contact support.");
    KhungController.performForcedLogoutFromServer();
  }

  @Override
  public void onAccountUnbanned() {
    showFxAlert(
        Alert.AlertType.INFORMATION,
        "Account Reinstated",
        "Your account is active again.",
        "An administrator has lifted your suspension. Please log in to continue.");
  }

  private static void showFxAlert(Alert.AlertType type, String title, String header, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
