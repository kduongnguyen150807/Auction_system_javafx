package com.auction.client.service.admin;

import com.auction.client.store.lotsinformation.PendingLots;
import com.auction.client.store.userinformation.UsersList;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

public class AdminService {
  public boolean lockUser(User user) {
    return RequestHelper.sendRequest(Request.LOCK_USER, user.getUsername())
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK)) {
          user.setLocked(true);
          UsersList.USER_LIST.updateUser(user);
          return true;
        } else {
          return false;
        }
      }).join();
  }

  public boolean unlockUser(User user) {
    return RequestHelper.sendRequest(Request.UNLOCK_USER, user.getUsername())
      .thenApply(response ->  {
        if (response.getStatus().equals(Response.OK)) {
          user.setLocked(false);
          UsersList.USER_LIST.updateUser(user);
          return true;
        } else {
          return false;
        }
      }).join();
  }

  public boolean toggleAdmin(User user, String newRole) {
    return RequestHelper.sendRequest(Request.PROMOTE_ADMIN, user.getUsername() + ":" + newRole)
      .thenApply(response ->  {
        if (response.getStatus().equals(Response.OK)) {
          return true;
        } else  {
          return false;
        }
      }).join();
  }

  public boolean approveItem(Item item) {
    return RequestHelper.sendRequest(Request.APPROVE_ITEM, item.getId())
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK)) {
          PendingLots.PENDING_LOTS.removeClientItem(item);
          return true;
        } else {
          return false;
        }
      }).join();
  }

  public boolean rejectItem(Item item) {
    return RequestHelper.sendRequest(Request.REJECT_ITEM, item.getId())
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK)) {
          PendingLots.PENDING_LOTS.removeClientItem(item);
          return true;
        } else {
          return false;
        }
      }).join();
  }
}
