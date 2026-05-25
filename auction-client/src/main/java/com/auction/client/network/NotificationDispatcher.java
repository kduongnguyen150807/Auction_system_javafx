package com.auction.client.network;

import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.service.user.AuthService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.userinformation.SelectedUser;
import com.auction.client.ui.homeview.homeviewcomponent.NotificationBell;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.FXThread;
import com.auction.shared.ChatMessage;
import com.auction.shared.Friendship;
import com.auction.shared.Item;
import com.auction.shared.User;

public class NotificationDispatcher implements NetworkEventListener{
  private final AuctionDetailService auctionDetailService;
  private final AuthService authService;
  private final AuctionDiscoveryService auctionDiscoveryService;

  private NotificationBell notificationBell;

  @Override
  public void setNotificationBell(NotificationBell notificationBell) {
    this.notificationBell = notificationBell;
  }

  public NotificationDispatcher(
    AuctionDetailService auctionDetailService,
    AuthService authService,
    AuctionDiscoveryService auctionDiscoveryService
  ) {
    this.auctionDetailService = auctionDetailService;
    this.authService = authService;
    this.auctionDiscoveryService = auctionDiscoveryService;
  }

  @Override
  public void onOutbidNotify(Item item) {
    addNotification(NotificationBell.OUT_BID, "You have been out bid in lot: " + item.getId() + " " + item.getName());
  }

  @Override
  public void onNewBidUpdate(Item item) {
    auctionDetailService.handleRealtimeItemUpdate(item);
  }

  @Override
  public void onItemClosed(Item item) {
    auctionDiscoveryService.getUserById(item.getWinnerId())
        .thenAccept(user -> {
          item.setWinnerUsername(user.getUsername());
          auctionDetailService.closeItem(item);
        });

  }

  @Override
  public void onAccountBanned(String reason) {
    AlertUtil.showErrorAlert("BAN", reason);
    addNotification(NotificationBell.WARN, reason);
    authService.signOut();
  }

  @Override
  public void onGlobalChat(ChatMessage message) {
    if (SelectedUser.SELECTED_USER.getSelectedUser() == null) {
      SelectedUser.SELECTED_USER.addMessage(message);
    }
  }

  @Override
  public void onPrivateChat(ChatMessage message) {
    if (SelectedUser.SELECTED_USER.getSelectedUser().getUser().getId() == message.getSenderId()) {
      SelectedUser.SELECTED_USER.addMessage(message);
    }
  }

  @Override
  public void onFriendRequest(Friendship friendship) {
    ClientSession.CURRENT_SESSION.getRequestList().getIdSet().add(friendship);
  }

  @Override
  public void onAccountUnbanned() {
    FXThread.run(() -> {
      AlertUtil.showInfoAlert("BAN", "You have been unbanned");
    });
  }

  public void addNotification(String icon, String message) {
    if (notificationBell != null) {
      notificationBell.addNotification(icon, message);
    }
  }
}
