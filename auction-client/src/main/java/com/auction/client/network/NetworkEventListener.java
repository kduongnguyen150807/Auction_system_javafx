package com.auction.client.network;

import com.auction.shared.ChatMessage;
import com.auction.shared.Friendship;
import com.auction.shared.Item;
import com.auction.shared.User;

public interface NetworkEventListener {
  default void onBalanceUpdate(User user) {}
  default void onOutbidNotify(int itemId) {}
  default void onNewBidUpdate(Item item) {}
  default void onItemClosed(Item item) {}
  default void onGlobalChat(ChatMessage message) {}
  default void onPrivateChat(ChatMessage message) {}
  default void onFriendRequest(Friendship friendship) {}
  default void onFriendRequestSent(Friendship friendship) {}
  default void onFriendAccepted(Friendship friendship) {}
  default void onSellerBidNotify(Item item, double newPrice) {}

  /**
   * Called when the server notifies this client that its account has been
   * banned. The UI should show a modal alert and then force-logout.
   *
   * @param reason human-readable reason string from the server
   */
  default void onAccountBanned(String reason) {}

  /**
   * Called when the server notifies this client that its account ban has been
   * lifted. The UI may show an informational toast or alert.
   */
  default void onAccountUnbanned() {}
}
