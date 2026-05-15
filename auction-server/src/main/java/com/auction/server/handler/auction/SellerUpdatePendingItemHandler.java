package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.auction.SettlementService;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class SellerUpdatePendingItemHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    User me = context.getCurrentUser();
    try {
      @SuppressWarnings("unchecked")
      Map<String, String> data = (Map<String, String>) request.getPayload();
      if (data == null || data.get("itemid") == null) {
        return new Response(request.getRequestId(), Response.ERROR, "invalid_payload", null);
      }
      int itemId = Integer.parseInt(data.get("itemid").trim());
      Item item = context.getItemDao().getById(itemId);
      if (item == null) {
        return new Response(request.getRequestId(), Response.ERROR, "not_found", null);
      }
      if (item.getSellerId() != me.getId()) {
        return new Response(request.getRequestId(), Response.ERROR, "forbidden", null);
      }
      LocalDateTime now = LocalDateTime.now();
      boolean pending = item.getStatus() == ItemStatus.PENDING;
      boolean openBeforeStart =
          item.getStatus() == ItemStatus.OPEN
              && item.getStartTime() != null
              && item.getStartTime().isAfter(now);
      if (!pending && !openBeforeStart) {
        return new Response(request.getRequestId(), Response.ERROR, "cannot_edit", null);
      }

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm[:ss]");
      String startTimeStr = data.get("starttime");
      if (startTimeStr == null) {
        return new Response(request.getRequestId(), Response.ERROR, "fail", null);
      }
      if (startTimeStr.length() == 16) startTimeStr += ":00";
      LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);

      String endTimeStr = data.get("endtime");
      if (endTimeStr == null) {
        return new Response(request.getRequestId(), Response.ERROR, "fail", null);
      }
      if (endTimeStr.length() == 16) endTimeStr += ":00";
      LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);

      AuctionType auctionType =
          AuctionType.parse(data.getOrDefault("auctiontype", "ENGLISH"));
      double starting = Double.parseDouble(data.get("startingprice"));

      double maxPrice = Double.parseDouble(data.getOrDefault("maxprice", "0"));
      double dutchReserve = 0;
      double dutchTickAmt = 0;
      int dutchIntervalMin = 0;

      if (auctionType == AuctionType.DUTCH) {
        maxPrice = 0;
        dutchReserve = Double.parseDouble(data.getOrDefault("dutchreserve", "0"));
        dutchTickAmt = Double.parseDouble(data.getOrDefault("dutchdecrement", "0"));
        dutchIntervalMin = Integer.parseInt(data.getOrDefault("dutchintervalmins", "0"));
        String err = validateDutch(starting, dutchReserve, dutchTickAmt, dutchIntervalMin);
        if (err != null) {
          return new Response(request.getRequestId(), Response.ERROR, err, null);
        }
      }

      if (openBeforeStart && !startTime.isAfter(now)) {
        return new Response(request.getRequestId(), Response.ERROR, "start_must_be_future", null);
      }
      if (!endTime.isAfter(startTime)) {
        return new Response(request.getRequestId(), Response.ERROR, "invalid_time_range", null);
      }

      boolean success =
          context
              .getItemDao()
              .updateSellerListingBeforeStartBySeller(
                  itemId,
                  me.getId(),
                  data.get("name"),
                  data.get("description"),
                  starting,
                  maxPrice,
                  startTime,
                  endTime,
                  data.getOrDefault("imageurl", ""),
                  data.getOrDefault("category", "Vehicle"),
                  auctionType,
                  dutchReserve,
                  dutchTickAmt,
                  dutchIntervalMin);

      if (success && openBeforeStart) {
        SettlementService.getInstance().schedule(itemId, endTime);
      }

      return new Response(
          request.getRequestId(),
          success ? Response.OK : Response.ERROR,
          success ? "success" : "fail",
          null);
    } catch (Exception e) {
      return new Response(request.getRequestId(), Response.ERROR, "fail", null);
    }
  }

  private static String validateDutch(double startPrice, double reserve, double tick, int mins) {
    if (reserve < 0) return "Invalid reserve price";
    if (reserve >= startPrice) return "Reserve must be below starting price";
    if (tick <= 0) return "Price decrement must be positive";
    if (mins <= 0) return "Decrease interval must be at least 1 minute";
    return null;
  }
}
