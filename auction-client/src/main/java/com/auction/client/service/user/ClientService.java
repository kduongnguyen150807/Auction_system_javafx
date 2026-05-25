package com.auction.client.service.user;

import com.auction.client.network.NetworkClient;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.clientinformation.UserTransactionHistory;
import com.auction.client.store.userinformation.SelectedUser;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.FXThread;
import com.auction.client.util.RequestHelper;
import com.auction.shared.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ClientService {

  /**
   * Cập nhật thông tin cá nhân bất đồng bộ
   */
  public CompletableFuture<Response> updateProfile(String fullName, String email, String phone) {
    Map<String, String> data = new HashMap<>();
    data.put("userid", String.valueOf(ClientSession.CURRENT_SESSION.getCurrentUser().getId()));
    data.put("fullname", fullName);
    data.put("email", email);
    data.put("phone", phone);

    return RequestHelper.sendRequest(Request.UPDATE_PROFILE, (Serializable) data)
      .thenApply(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          ClientSession.CURRENT_SESSION.applyProfileUpdate(fullName, email, phone);
        }
        return response;
      });
  }

  public CompletableFuture<Response> deposit(double amount) {
    Map<String, String> data = new HashMap<>();
    data.put("userid", String.valueOf(ClientSession.CURRENT_SESSION.getCurrentUser().getId()));
    data.put("amount", String.valueOf(amount));

    return RequestHelper.sendRequest(Request.DEPOSIT, (Serializable) data)
      .thenApply(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          ClientSession.CURRENT_SESSION.deposit(amount);
        }
        return response;
      });
  }

  public CompletableFuture<Response> updateAvatar(String username, String avatarUrl) {
    return RequestHelper.sendRequest(Request.UPDATE_AVATAR, username + " " + avatarUrl)
      .thenApply(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          ClientSession.CURRENT_SESSION.avatarUrlProperty().set(avatarUrl);
        }
        return response;
      });
  }

  public CompletableFuture<Response> uploadImage(String uploadUrl, byte[] imageBytes) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return NetworkClient.uploadFile(uploadUrl, imageBytes);
      } catch (Exception e) {
        throw new RuntimeException("Lỗi tải tệp tin lên máy chủ", e);
      }
    }).thenCompose(url -> updateAvatar(ClientSession.CURRENT_SESSION.getCurrentUser().getUsername(), url));
  }

  @SuppressWarnings("unchecked")
  public CompletableFuture<Void> getUserTransaction() {
    // BỎ .join()
    return RequestHelper.sendRequest(Request.GET_TRANSACTIONS, ClientSession.CURRENT_SESSION.getCurrentUser().getId())
      .thenAccept(response -> {
        if (response != null && Response.OK.equals(response.getStatus()) && response.getPayload() instanceof List<?> list) {
          UserTransactionHistory.USER_TRANSACTION_HISTORY.setHistory((List<TransactionLog>) list);
        } else {
          throw new RuntimeException(response.getMessage() != null ? response.getMessage() : "Failed to get user transaction");
        }
      });
  }

  public CompletableFuture<List<Item>> getUserItem() {
    return RequestHelper.sendRequest(Request.GET_MY_ITEMS, ClientSession.CURRENT_SESSION.getCurrentUser().getId())
      .thenApply(response ->  {
        if (response != null && Response.OK.equals(response.getStatus())) {
          List<Item> items = (List<Item>) response.getPayload();
          return items;
        }
        return List.of();
      });
  }

  public CompletableFuture<Void> getWatchedList() {
    return RequestHelper.sendRequest(Request.GET_WATCHLIST, ClientSession.CURRENT_SESSION.getCurrentUser().getId())
      .thenAccept(response ->  {
        if (response != null && Response.OK.equals(response.getStatus())) {
          List<Integer> watchedItems = (List<Integer>) response.getPayload();
          ClientSession.CURRENT_SESSION.getWatchedItemsList().initialize(watchedItems);
        } else {
          throw new RuntimeException(response.getMessage() != null ? response.getMessage() : "Failed to get watched list");
        }
      });
  }

  public CompletableFuture<List<Item>> getWatchedItems() {
    return RequestHelper.sendRequest(Request.GET_WATCHLIST_ITEMS, ClientSession.CURRENT_SESSION.getCurrentUser().getId())
      .thenApply(response ->   {
        if (response != null && Response.OK.equals(response.getStatus())) {
          List<Item> items = (List<Item>) response.getPayload();
          return items;
        } else {
          throw new RuntimeException(response.getMessage() != null ? response.getMessage() : "Failed to get watched list");
        }
      });
  }

  public CompletableFuture<Void> getFriendsList() {
    return RequestHelper.sendRequest(Request.GET_FRIENDS, ClientSession.CURRENT_SESSION.getCurrentUser().getId())
      .thenAccept(response ->   {
        if (response != null && Response.OK.equals(response.getStatus())) {
          List<Friendship> friends = (List<Friendship>) response.getPayload();
          ClientSession.CURRENT_SESSION.getFriendsList().initialize(friends);
        } else {
          ClientSession.CURRENT_SESSION.getFriendsList().initialize(List.of());
        }
      });
  }

  public CompletableFuture<Void> getFriendRequest() {
    return RequestHelper.sendRequest(Request.GET_FRIEND_REQUESTS, ClientSession.CURRENT_SESSION.getCurrentUser().getId())
      .thenAccept(response ->  {
        if (response != null && Response.OK.equals(response.getStatus())) {
          List<Friendship> friendRequests = (List<Friendship>) response.getPayload();
          ClientSession.CURRENT_SESSION.getRequestList().initialize(friendRequests);
        } else  {
          ClientSession.CURRENT_SESSION.getRequestList().initialize(List.of());
        }
      });
  }

  public CompletableFuture<Void> acceptFriendRequest(int id) {
    return RequestHelper.sendRequest(Request.ACCEPT_FRIEND, id)
      .thenAccept(response ->  {
        if (response != null && Response.OK.equals(response.getStatus())) {
          FXThread.run(() -> {
            AlertUtil.showInfoAlert("FRIEND", "FRIEND ACCEPTED");
            ClientSession.CURRENT_SESSION.getRequestList().getIdSet().removeIf(
              param -> param.getRequesterId() == id
            );
          });
        }
      });
  }

  public CompletableFuture<Void> declineFriendRequest(int id) {
    return RequestHelper.sendRequest(Request.DECLINE_FRIEND, id)
      .thenAccept(response ->  {
        if (response != null && Response.OK.equals(response.getStatus())) {
          FXThread.run(() -> {
            AlertUtil.showInfoAlert("FRIEND", "FRIEND ACCEPTED");
            ClientSession.CURRENT_SESSION.getRequestList().getIdSet().removeIf(
              param -> param.getRequesterId() == id
            );
          });
        }
      });
  }

  public CompletableFuture<Void> toggleWatchedItem(int itemId, boolean isWatching) {
    Map<String, Object> data = new HashMap<>();
    data.put("itemId", itemId);
    data.put("isWatching", isWatching);
    return RequestHelper.sendRequest(Request.TOGGLE_WATCHLIST, data)
      .thenAccept(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          ClientSession.CURRENT_SESSION.getWatchedItemsList().toggle(itemId, isWatching);
        } else {
          throw new RuntimeException(response.getMessage() != null ? response.getMessage() : "Failed to toggle watched list");
        }
      });
  }

  public CompletableFuture<Void> getChatHistory(int otherId) {
    Map<String, Object> data = new HashMap<>();
    data.put("myId", ClientSession.CURRENT_SESSION.getCurrentUser().getId());
    data.put("otherId", otherId);
    return RequestHelper.sendRequest(Request.GET_PRIVATE_CHAT_HISTORY, (Serializable) data)
      .thenAccept(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          SelectedUser.SELECTED_USER.setSelectedChatHistory((List<ChatMessage>) response.getPayload());
        } else {
          SelectedUser.SELECTED_USER.setSelectedChatHistory(List.of());
        }
      });
  }

  public CompletableFuture<Void> sendMessage(ChatMessage chatMessage) {
    return RequestHelper.sendRequest(Request.SEND_CHAT, chatMessage)
      .thenAccept(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          if (chatMessage.getReceiverId() == SelectedUser.SELECTED_USER.getSelectedUser().getUser().getId()) {
            FXThread.run(() -> {
              SelectedUser.SELECTED_USER.addMessage(chatMessage);
            });
          }
        } else {
          AlertUtil.showInfoAlert("CHAT", "Failed to send message");
        }
      });
  }

  public CompletableFuture<Void> getGlobalChatHistory() {
    return RequestHelper.sendRequest(Request.GET_GLOBAL_CHAT_HISTORY, null)
      .thenAccept(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          SelectedUser.SELECTED_USER.setSelectedChatHistory((List<ChatMessage>) response.getPayload());
        } else  {
          FXThread.run(() -> {
            AlertUtil.showInfoAlert("CHAT", "Failed to fetch global chat history");
          });
        }
      });
  }

  public CompletableFuture<Void> refreshUserTransaction() {
    return getUserTransaction();
  }
}