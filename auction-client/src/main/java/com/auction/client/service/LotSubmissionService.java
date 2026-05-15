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

  /** Sends {@link Request#ADD_LOT}; caller checks status and {@link Response#getMessage()}. */
  public Response submitLot(Map<String, String> lotData) {
    Request request = new Request(Request.ADD_LOT, (Serializable) lotData);
    return NetworkClient.getInstance().sendRequestAndWait(request);
  }

  /** Updates a seller-owned listing before it goes live ({@code PENDING} or upcoming {@code OPEN}). */
  public Response updatePendingLot(Map<String, String> lotData) {
    Request request = new Request(Request.SELLER_UPDATE_PENDING_ITEM, (Serializable) lotData);
    return NetworkClient.getInstance().sendRequestAndWait(request);
  }

  /** Cancels PENDING or OPEN listing owned by the seller. */
  public Response cancelSellerItem(int itemId) {
    Request request =
        new Request(
            Request.SELLER_CANCEL_ITEM, (Serializable) java.util.Map.of("itemid", String.valueOf(itemId)));
    return NetworkClient.getInstance().sendRequestAndWait(request);
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
