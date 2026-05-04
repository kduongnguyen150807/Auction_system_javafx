package com.auction.server.handler;

import com.auction.shared.*;
import java.util.List;

public class RatingHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();

    if (action.equals(Request.GET_RATINGS)) {
      int itemId = (int) request.getPayload();
      List<Rating> ratings = context.getRatingDao().getByItemId(itemId);
      return new Response(requestId, Response.OK, "success", (java.io.Serializable) ratings);
    }

    if (action.equals(Request.SUBMIT_RATING)) {
      return handleSubmitRating(request, context);
    }

    return new Response(requestId, Response.ERROR, "unknown_action", null);
  }

  private Response handleSubmitRating(Request request, HandlerContext context) {
    String requestId = request.getRequestId();
    try {
      Rating rating = (Rating) request.getPayload();
      Item item = context.getItemDao().getById(rating.getItemId());
      if (item == null) {
        return new Response(requestId, Response.ERROR, "item_not_found", null);
      }
      if (item.getStatus() != ItemStatus.CLOSED && item.getStatus() != ItemStatus.FINISHED) {
        return new Response(requestId, Response.ERROR, "auction_not_ended", null);
      }

      int currentUserId = context.getCurrentUser().getId();
      if (currentUserId != item.getWinnerId() && currentUserId != item.getSellerId()) {
        return new Response(requestId, Response.ERROR, "not_participant", null);
      }
      if (context.getRatingDao().hasRated(rating.getItemId(), currentUserId)) {
        return new Response(requestId, Response.ERROR, "already_rated", null);
      }

      rating.setRaterUserId(currentUserId);
      if (currentUserId == item.getWinnerId()) {
        rating.setRatedUserId(item.getSellerId());
      } else {
        rating.setRatedUserId(item.getWinnerId());
      }

      boolean success = context.getRatingDao().insertRating(rating);
      if (success) {
        context.getRatingDao().recalcUserRating(rating.getRatedUserId());
        return new Response(requestId, Response.OK, "success", null);
      }
      return new Response(requestId, Response.ERROR, "fail", null);
    } catch (Exception e) {
      return new Response(requestId, Response.ERROR, "fail", null);
    }
  }
}
