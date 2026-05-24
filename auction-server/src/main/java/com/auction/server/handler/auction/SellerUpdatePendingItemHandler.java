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
import java.time.format.DateTimeParseException;
import java.util.Map;

public class SellerUpdatePendingItemHandler implements ActionHandler {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
          DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm[:ss]");

  @Override
  public Response handle(Request request, HandlerContext context) {
    User me = context.getCurrentUser();

    try {
      @SuppressWarnings("unchecked")
      Map<String, String> data = (Map<String, String>) request.getPayload();

      String payloadError = validateUpdatePayload(data);
      if (payloadError != null) {
        return new Response(request.getRequestId(), Response.ERROR, payloadError, null);
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

      LocalDateTime startTime = parseDateTime(data.get("starttime"));
      LocalDateTime endTime = parseDateTime(data.get("endtime"));

      if (openBeforeStart && !startTime.isAfter(now)) {
        return new Response(request.getRequestId(), Response.ERROR, "start_must_be_future", null);
      }

      if (!endTime.isAfter(startTime)) {
        return new Response(request.getRequestId(), Response.ERROR, "invalid_time_range", null);
      }

      AuctionType auctionType =
              AuctionType.parse(data.getOrDefault("auctiontype", "ENGLISH"));

      double startingPrice = Double.parseDouble(data.get("startingprice"));
      if (startingPrice <= 0) {
        return new Response(request.getRequestId(), Response.ERROR, "invalid_starting_price", null);
      }

      double maxPrice = Double.parseDouble(data.getOrDefault("maxprice", "0"));
      double dutchReserve = 0;
      double dutchTickAmount = 0;
      int dutchIntervalMinutes = 0;

      if (auctionType == AuctionType.DUTCH) {
        maxPrice = 0;
        dutchReserve = Double.parseDouble(data.getOrDefault("dutchreserve", "0"));
        dutchTickAmount = Double.parseDouble(data.getOrDefault("dutchdecrement", "0"));
        dutchIntervalMinutes = Integer.parseInt(data.getOrDefault("dutchintervalmins", "0"));

        String dutchError =
                validateDutch(startingPrice, dutchReserve, dutchTickAmount, dutchIntervalMinutes);

        if (dutchError != null) {
          return new Response(request.getRequestId(), Response.ERROR, dutchError, null);
        }
      }

      boolean success =
              context
                      .getItemDao()
                      .updateSellerListingBeforeStartBySeller(
                              itemId,
                              me.getId(),
                              data.get("name"),
                              data.get("description"),
                              startingPrice,
                              maxPrice,
                              startTime,
                              endTime,
                              data.getOrDefault("imageurl", ""),
                              data.getOrDefault("category", "Vehicle"),
                              auctionType,
                              dutchReserve,
                              dutchTickAmount,
                              dutchIntervalMinutes);

      if (success && openBeforeStart) {
        SettlementService.getInstance().schedule(itemId, endTime);
      }

      return new Response(
              request.getRequestId(),
              success ? Response.OK : Response.ERROR,
              success ? "success" : "fail",
              null);
    } catch (ClassCastException e) {
      return new Response(request.getRequestId(), Response.ERROR, "invalid_payload", null);
    } catch (NumberFormatException e) {
      return new Response(request.getRequestId(), Response.ERROR, "invalid_number_format", null);
    } catch (DateTimeParseException e) {
      return new Response(request.getRequestId(), Response.ERROR, "invalid_datetime_format", null);
    } catch (Exception e) {
      return new Response(request.getRequestId(), Response.ERROR, "server_error", null);
    }
  }

  private static String validateUpdatePayload(Map<String, String> data) {
    if (data == null) {
      return "invalid_payload";
    }

    if (isBlank(data.get("itemid"))
            || isBlank(data.get("name"))
            || isBlank(data.get("description"))
            || isBlank(data.get("startingprice"))
            || isBlank(data.get("starttime"))
            || isBlank(data.get("endtime"))) {
      return "missing_required_fields";
    }

    return null;
  }

  private static LocalDateTime parseDateTime(String value) {
    String normalized = value.trim();

    if (normalized.length() == 16) {
      normalized += ":00";
    }

    return LocalDateTime.parse(normalized, DATE_TIME_FORMATTER);
  }

  private static String validateDutch(double startPrice, double reserve, double tick, int mins) {
    if (reserve < 0) {
      return "Invalid reserve price";
    }

    if (reserve >= startPrice) {
      return "Reserve must be below starting price";
    }

    if (tick <= 0) {
      return "Price decrement must be positive";
    }

    if (mins <= 0) {
      return "Decrease interval must be at least 1 minute";
    }

    return null;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}