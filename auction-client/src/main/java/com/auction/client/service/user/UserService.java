package com.auction.client.service.user;

import com.auction.client.store.userinformation.UsersList;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserService {

  @SuppressWarnings("unchecked")
  public CompletableFuture<Void> getAllUsers() {
    return RequestHelper.sendRequest(Request.GET_ALL_USERS, null)
      .thenAccept(response -> {
        if (Response.OK.equals(response.getStatus()) && response.getPayload() instanceof List<?> rawList) {
          List<User> users = (List<User>) rawList;
          UsersList.USER_LIST.setUsers(users);
        } else {
          throw new RuntimeException(response.getMessage() != null ? response.getMessage() : "Failed to fetch all users");
        }
      });
  }

  public CompletableFuture<User> getUserById(int id) {
    return RequestHelper.sendRequest(Request.GET_USER_BY_ID, id)
      .thenApply(response ->  {
        if (Response.OK.equals(response.getStatus()) && response.getPayload() instanceof User) {
          return (User) response.getPayload();
        }
        return null;
      });
  }
}