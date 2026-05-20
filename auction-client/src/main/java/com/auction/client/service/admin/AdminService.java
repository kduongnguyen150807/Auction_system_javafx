package com.auction.client.service.admin;

import com.auction.client.store.lotsinformation.PendingLots;
import com.auction.client.store.userinformation.UsersList;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class AdminService {
  public CompletableFuture<Boolean> lockUser(User user) {
    return RequestHelper.sendRequest(Request.LOCK_USER, user.getUsername())
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK)) {
          user.setLocked(true);
          UsersList.USER_LIST.updateUser(user);
          return true;
        } else {
          return false;
        }
      });
  }

  public CompletableFuture<Boolean> unlockUser(User user) {
    return RequestHelper.sendRequest(Request.UNLOCK_USER, user.getUsername())
      .thenApply(response ->  {
        if (response.getStatus().equals(Response.OK)) {
          user.setLocked(false);
          UsersList.USER_LIST.updateUser(user);
          return true;
        } else {
          return false;
        }
      });
  }

  public CompletableFuture<Boolean> toggleAdmin(User user, String newRole) {
    return RequestHelper.sendRequest(Request.PROMOTE_ADMIN, user.getUsername() + ":" + newRole)
      .thenApply(response ->  {
        if (response.getStatus().equals(Response.OK)) {
          return true;
        } else  {
          return false;
        }
      });
  }

  public CompletableFuture<Boolean> approveItem(Item item) {
    return RequestHelper.sendRequest(Request.APPROVE_ITEM, item.getId())
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK)) {
          PendingLots.PENDING_LOTS.removeClientItem(item);
          return true;
        } else {
          return false;
        }
      });
  }

  public CompletableFuture<Boolean> rejectItem(Item item) {
    return RequestHelper.sendRequest(Request.REJECT_ITEM, item.getId())
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK)) {
          PendingLots.PENDING_LOTS.removeClientItem(item);
          return true;
        } else {
          return false;
        }
      });
  }

  @SuppressWarnings("unchecked")
  public CompletableFuture<HashMap<String, Integer>> getStatusStats() {
    return RequestHelper.sendRequest("get_status_stats", null)
      .thenApply(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          return (HashMap<String, Integer>) response.getPayload();
        }
        throw new RuntimeException(response != null ? response.getMessage() : "Failed to fetch status stats");
      });
  }

  @SuppressWarnings("unchecked")
  public CompletableFuture<HashMap<String, Double>> getCategoryStats() {
    return RequestHelper.sendRequest("get_category_stats", null)
      .thenApply(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          return (HashMap<String, Double>) response.getPayload();
        }
        throw new RuntimeException(response != null ? response.getMessage() : "Failed to fetch category stats");
      });
  }
}
