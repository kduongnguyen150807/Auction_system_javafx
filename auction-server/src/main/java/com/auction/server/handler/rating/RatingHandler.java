package com.auction.server.handler.rating;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.*;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RatingHandler implements ActionHandler {
  private static final Logger LOGGER = Logger.getLogger(RatingHandler.class.getName());

  @Override
  public Response handle(Request request, HandlerContext context) {
    if (request == null) {
      return new Response(null, Response.ERROR, "null_request", null);
    }

    String action = request.getAction();
    String requestId = request.getRequestId();

    try {
      if (Request.GET_RATINGS.equals(action)) {
        return handleGetRatings(requestId, request.getPayload(), context);
      }

      if (Request.SUBMIT_RATING.equals(action)) {
        return handleSubmitRating(request, context);
      }

      return error(requestId, "unknown_action");
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error while handling rating action: " + action, e);
      return error(requestId, "fail");
    }
  }

  private Response handleGetRatings(String requestId, Object payload, HandlerContext context) {
    if (!(payload instanceof Integer)) {
      return error(requestId, "invalid_payload");
    }

    int itemId = (Integer) payload;
    List<Rating> ratings = context.getRatingDao().getByItemId(itemId);

    return ok(requestId, "success", (Serializable) ratings);
  }

  private Response handleSubmitRating(Request request, HandlerContext context) {
    String requestId = request.getRequestId();
    Object payload = request.getPayload();

    if (!(payload instanceof Rating)) {
      return error(requestId, "invalid_payload");
    }

    Rating rating = (Rating) payload;

    Item item = context.getItemDao().getById(rating.getItemId());

    if (item == null) {
      return error(requestId, "item_not_found");
    }

    if (item.getStatus() != ItemStatus.CLOSED && item.getStatus() != ItemStatus.FINISHED) {
      return error(requestId, "auction_not_ended");
    }

    int currentUserId = context.getCurrentUser().getId();

    if (currentUserId != item.getWinnerId()) {
      return error(requestId, "not_buyer");
    }

    if (context.getRatingDao().hasRated(rating.getItemId(), currentUserId)) {
      return error(requestId, "already_rated");
    }

    rating.setRaterUserId(currentUserId);
    rating.setRatedUserId(item.getSellerId());

    boolean success = context.getRatingDao().insertRating(rating);

    if (success) {
      context.getRatingDao().recalcUserRating(rating.getRatedUserId());
      return ok(requestId, "success", null);
    }

    return error(requestId, "fail");
  }

  private Response ok(String requestId, String message, Serializable payload) {
    return new Response(requestId, Response.OK, message, payload);
  }

  private Response error(String requestId, String message) {
    return new Response(requestId, Response.ERROR, message, null);
  }
}