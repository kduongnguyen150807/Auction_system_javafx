package com.auction.client.service;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.SpinWheelResult;
import com.auction.shared.SpinWheelState;
import com.auction.shared.User;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SpinWheelClientService {

  public SpinWheelState fetchState() {
    Response response =
        NetworkClient.getInstance().sendRequestAndWait(new Request(Request.GET_SPIN_WHEEL_STATE, null));
    if (response != null && Response.OK.equals(response.getStatus())) {
      return (SpinWheelState) response.getPayload();
    }
    return null;
  }

  public SpinWheelResult spin() {
    Response response =
        NetworkClient.getInstance().sendRequestAndWait(new Request(Request.SPIN_WHEEL, null));
    if (response == null) {
      return null;
    }
    SpinWheelResult result = (SpinWheelResult) response.getPayload();
    if (Response.OK.equals(response.getStatus()) && result != null && result.getUser() != null) {
      ClientSession.setCurrentUser(result.getUser());
    }
    return result;
  }

  public SpinWheelState buyCredits(int count) {
    Map<String, Integer> data = new HashMap<>();
    data.put("count", count);
    Response response =
        NetworkClient.getInstance()
            .sendRequestAndWait(new Request(Request.BUY_SPIN_CREDITS, (Serializable) data));
    if (response != null && Response.OK.equals(response.getStatus())) {
      User me = ClientSession.getCurrentUser();
      if (me != null) {
        User refreshed = new UserAccountService().refreshUser(me.getId());
        if (refreshed != null) {
          ClientSession.setCurrentUser(refreshed);
        }
      }
      return (SpinWheelState) response.getPayload();
    }
    return null;
  }
}
