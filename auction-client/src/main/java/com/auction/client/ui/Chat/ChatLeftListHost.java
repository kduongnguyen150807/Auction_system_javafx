package com.auction.client.ui.Chat;

/**
 * Host callbacks for {@link ChatLeftListCell} so list rendering stays out of {@link
 * ChatPageController}.
 */
interface ChatLeftListHost {

  ChatSidebarTab currentSidebarTab();

  int currentUserId();

  void acceptFriend(int requesterId);

  void declineFriend(int requesterId);

  void addFriend(int userId);
}
