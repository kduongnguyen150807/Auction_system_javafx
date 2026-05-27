package com.auction.client.ui.AddNewLot;

import com.auction.client.ClientSession;
import com.auction.client.service.LotSubmissionService;
import com.auction.shared.Response;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;

/** Submits or updates a lot on a background thread. */
final class AddLotSubmissionCoordinator {

  record FormPayload(
      String name,
      String startingPrice,
      String maxPrice,
      String description,
      String category,
      String startTime,
      String endTime,
      boolean dutch,
      String dutchReserve,
      String dutchDecrement,
      String dutchIntervalMinutes,
      String imageUrl,
      Integer editingItemId) {}

  interface ResultHandler {
    void onEditSuccess();

    void onCreateSuccess();

    void onFailure(String message);
  }

  private final LotSubmissionService lotSubmissionService;

  AddLotSubmissionCoordinator(LotSubmissionService lotSubmissionService) {
    this.lotSubmissionService = lotSubmissionService;
  }

  void submitAsync(FormPayload payload, ResultHandler handler) {
    Thread thread =
        new Thread(
            () -> {
              try {
                Map<String, String> data = toRequestMap(payload);
                if (payload.editingItemId() != null) {
                  data.put("itemid", String.valueOf(payload.editingItemId()));
                  Response res = lotSubmissionService.updatePendingLot(data);
                  Platform.runLater(
                      () -> {
                        if (res != null && Response.OK.equals(res.getStatus())) {
                          handler.onEditSuccess();
                        } else {
                          handler.onFailure(
                              AddLotErrorMessages.format(res != null ? res.getMessage() : "fail"));
                        }
                      });
                  return;
                }
                data.put("sellerusername", ClientSession.getUsername());
                Response res = lotSubmissionService.submitLot(data);
                Platform.runLater(
                    () -> {
                      if (res != null && Response.OK.equals(res.getStatus())) {
                        handler.onCreateSuccess();
                      } else {
                        handler.onFailure(
                            AddLotErrorMessages.format(res != null ? res.getMessage() : "fail"));
                      }
                    });
              } catch (Exception ex) {
                Platform.runLater(() -> handler.onFailure("error"));
              }
            });
    thread.setDaemon(true);
    thread.start();
  }

  private static Map<String, String> toRequestMap(FormPayload payload) {
    Map<String, String> data = new HashMap<>();
    data.put("name", payload.name());
    data.put("startingprice", payload.startingPrice());
    data.put("auctiontype", payload.dutch() ? "DUTCH" : "ENGLISH");
    if (payload.dutch()) {
      data.put("maxprice", "0");
      data.put("dutchreserve", payload.dutchReserve());
      data.put("dutchdecrement", payload.dutchDecrement());
      data.put("dutchintervalmins", payload.dutchIntervalMinutes());
    } else {
      data.put("maxprice", payload.maxPrice());
    }
    data.put("description", payload.description());
    data.put("starttime", payload.startTime());
    data.put("endtime", payload.endTime());
    data.put("category", payload.category());
    data.put("imageurl", payload.imageUrl());
    return data;
  }
}
