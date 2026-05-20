package com.auction.client.service.user;

import com.auction.client.network.NetworkClient;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.clientinformation.UserTransactionHistory;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.TransactionLog;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientService {
  public String updateProfile(String fullName, String email, String phone) {
    Map<String, String> data = new HashMap<>();
    data.put("userid", String.valueOf(ClientSession.CURRENT_SESSION.getCurrentUser().getId()));
    data.put("fullname", fullName);
    data.put("email", email);
    data.put("phone", phone);
    return RequestHelper.sendRequest(Request.UPDATE_PROFILE, (Serializable) data)
      .thenApply(response -> {
        if (response != null && response.getStatus().equals(Response.OK)) {
          ClientSession.CURRENT_SESSION.applyProfileUpdate(fullName, email, phone);
          return null;
        } else {
          return "Fail to update profile";
        }
      }).join();
  }

  public String deposit(double amount) {
    Map<String, String> data = new HashMap<>();
    data.put("userid", String.valueOf(ClientSession.CURRENT_SESSION.getCurrentUser().getId()));
    data.put("amount", String.valueOf(amount));
    return RequestHelper.sendRequest(Request.DEPOSIT, (Serializable) data)
      .thenApply(response -> {
        if (response != null && response.getStatus().equals(Response.OK)) {
          ClientSession.CURRENT_SESSION.deposit(amount);
          return null;
        } else {
          return "Fail to deposit amount";
        }
      }).join();
  }

  public String updateAvatar(String username, String avatarUrl) {
    Request request = new Request(Request.UPDATE_AVATAR, username + " " + avatarUrl);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      ClientSession.CURRENT_SESSION.avatarUrlProperty().set(avatarUrl);
      return null;
    }
    return "fail to update avatar";
  }

  public String uploadImage(String uploadUrl, byte[] imageBytes) {
    try {
      String url = NetworkClient.uploadFile(uploadUrl, imageBytes);
      return updateAvatar(ClientSession.CURRENT_SESSION.getCurrentUser().getUsername(), url);
    } catch (Exception e) {
      return null;
    }
  }

  public String getUserTransaction() {
    return RequestHelper.sendRequest("get_transactions", ClientSession.CURRENT_SESSION.getCurrentUser().getId())
      .thenApply(response ->  {
        if (response != null && response.getStatus().equals(Response.OK)) {
          List<TransactionLog> list = (List<TransactionLog>) response.getPayload();
          UserTransactionHistory.USER_TRANSACTION_HISTORY.setHistory(list);
          return null;
        }
        return "failed to get user transaction";
      }).join();
  }

  public void refreshUserTransaction() {
    getUserTransaction();
  }
}
