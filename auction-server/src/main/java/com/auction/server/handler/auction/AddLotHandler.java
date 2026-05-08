package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

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

      boolean success = context.getItemDao().insertLot(
          data.get("name"),
          data.get("description"),
          Double.parseDouble(data.get("startingprice")),
          Double.parseDouble(data.getOrDefault("maxprice", "0")),
          startTime,
          endTime,
          data.get("sellerusername"),
          data.getOrDefault("imageurl", ""),
          data.getOrDefault("category", "Vehicle"));

      return new Response(request.getRequestId(),
          success ? Response.OK : Response.ERROR,
          success ? "success" : "fail", null);
    } catch (Exception e) {
      return new Response(request.getRequestId(), Response.ERROR, "fail", null);
    }
  }
}
