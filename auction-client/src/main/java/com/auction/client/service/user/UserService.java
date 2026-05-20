package com.auction.client.service.user;

import com.auction.client.store.userinformation.UsersList;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.util.List;

public class UserService {
  public boolean getAllUsers() {
    return RequestHelper.sendRequest(Request.GET_ALL_USERS, null)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> list) {
          UsersList.USER_LIST.setUsers((List<User>) list);
          return true;
        } else {
          return false;
        }
      }).join();
  }
}
