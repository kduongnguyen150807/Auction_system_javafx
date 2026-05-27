package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

import com.auction.shared.AuctionType;
import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AddLotHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    try {
      Map<String, String> data = (Map<String, String>) request.getPayload();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm[:ss]");

      String startTimeStr = data.get("starttime");
      if (startTimeStr.length() == 16) startTimeStr += ":00";
      LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);

      String endTimeStr = data.get("endtime");
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
        dutchIntervalMin = Integer.parseInt(data.getOrDefault("dutchintervalmins", "0"));
        String err =
            DutchAuctionPricing.validateDutchScheduleFromInterval(
                startTime, endTime, starting, dutchReserve, dutchIntervalMin);
        if (err != null) {
          return new Response(request.getRequestId(), Response.ERROR, err, null);
        }
        dutchTickAmt =
            DutchAuctionPricing.derivedTickAmount(
                startTime, endTime, dutchIntervalMin, starting, dutchReserve);
      } else if (!endTime.isAfter(startTime)) {
        return new Response(request.getRequestId(), Response.ERROR, "invalid_time_range", null);
      }

      boolean success =
          context
              .getItemDao()
              .insertLot(
                  data.get("name"),
                  data.get("description"),
                  starting,
                  maxPrice,
                  startTime,
                  endTime,
                  data.get("sellerusername"),
                  data.getOrDefault("imageurl", ""),
                  data.getOrDefault("category", "Vehicle"),
                  auctionType,
                  dutchReserve,
                  dutchTickAmt,
                  dutchIntervalMin);

      return new Response(request.getRequestId(),
          success ? Response.OK : Response.ERROR,
          success ? "success" : "fail", null);
    } catch (Exception e) {
      return new Response(request.getRequestId(), Response.ERROR, "fail", null);
    }
  }
}
