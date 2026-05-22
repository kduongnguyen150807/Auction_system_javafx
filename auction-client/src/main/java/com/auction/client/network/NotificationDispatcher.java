package com.auction.client.network;

import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.service.user.AuthService;
import com.auction.client.ui.homeview.homeviewcomponent.NotificationBell;
import com.auction.client.util.AlertUtil;
import com.auction.shared.Item;

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
    auctionDetailService.handleRealtimeItemUpdate(item);
  }

  @Override
  public void onAccountBanned(String reason) {
    AlertUtil.showErrorAlert("BAN", reason);
    addNotification(NotificationBell.WARN, reason);
    authService.signOut();
  }

  public void addNotification(String icon, String message) {
    if (notificationBell != null) {
      notificationBell.addNotification(icon, message);
    }
  }
}
