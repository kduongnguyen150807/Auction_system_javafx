package com.auction.client.network;

import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.service.user.AuthService;
import com.auction.client.util.AlertUtil;
import com.auction.shared.Item;

public class NotificationDispatcher implements NetworkEventListener{
  private final AuctionDetailService auctionDetailService;
  private final AuthService authService;
  private final AuctionDiscoveryService auctionDiscoveryService;

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
    authService.signOut();
  }
}
