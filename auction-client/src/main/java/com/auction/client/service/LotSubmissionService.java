package com.auction.client.service;

import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.io.Serializable;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LotSubmissionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(LotSubmissionService.class);

  public boolean submitLot(Map<String, String> lotData) {
    Request request = new Request(Request.ADD_LOT, (Serializable) lotData);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    return response != null && Response.OK.equals(response.getStatus());
  }

  public String uploadImage(String uploadUrl, byte[] imageBytes) {
    try {
      return NetworkClient.uploadFile(uploadUrl, imageBytes);
    } catch (Exception e) {
      LOGGER.warn("Image upload failed", e);
      return null;
    }
  }
}
