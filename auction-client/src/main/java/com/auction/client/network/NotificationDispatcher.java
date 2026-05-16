package com.auction.client.network;

import com.auction.client.service.AuctionService;
import com.auction.shared.Item;

public class NotificationDispatcher implements NetworkEventListener{
  @Override
  public void onNewBidUpdate(Item item) {
    AuctionService.updateItem(item);
  }

  @Override
  public void onItemClosed(Item item) {
    AuctionService.updateItem(item);
  }
}
