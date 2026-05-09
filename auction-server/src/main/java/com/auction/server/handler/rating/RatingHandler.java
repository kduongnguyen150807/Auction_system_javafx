package com.auction.server.handler.rating;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.*;
import java.util.List;

public class RatingHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestid = request.getRequestId();
    if (action.equals(Request.GET_RATINGS)) {
      int itemid = (int) request.getPayload();
      List<Rating> ratings = context.getRatingDao().getByItemId(itemid);
      Response ans = new Response(requestid, Response.OK, "success", (java.io.Serializable) ratings);
      return ans;
    }
    if (action.equals(Request.SUBMIT_RATING)) {
      Response ans = handlesubmitrating(request, context);
      return ans;
    }
    Response res = new Response(requestid, Response.ERROR, "unknown_action", null);
    return res;
  }

  private Response handlesubmitrating(Request request, HandlerContext context) {
    String requestid = request.getRequestId();
    try {
      Rating rating = (Rating) request.getPayload();
      Item item = context.getItemDao().getById(rating.getItemId());
      if (item == null) {
        Response res = new Response(requestid, Response.ERROR, "item_not_found", null);
        return res;
      }
      if (item.getStatus() != ItemStatus.CLOSED && item.getStatus() != ItemStatus.FINISHED) {
        Response res = new Response(requestid, Response.ERROR, "auction_not_ended", null);
        return res;
      }
      int currentuserid = context.getCurrentUser().getId();
      if (currentuserid != item.getWinnerId()) {
        Response res = new Response(requestid, Response.ERROR, "not_buyer", null);
        return res;
      }
      if (context.getRatingDao().hasRated(rating.getItemId(), currentuserid)) {
        Response res = new Response(requestid, Response.ERROR, "already_rated", null);
        return res;
      }
      rating.setRaterUserId(currentuserid);
      rating.setRatedUserId(item.getSellerId());
      boolean success = context.getRatingDao().insertRating(rating);
      if (success) {
        context.getRatingDao().recalcUserRating(rating.getRatedUserId());
        Response res = new Response(requestid, Response.OK, "success", null);
        return res;
      }
      Response res = new Response(requestid, Response.ERROR, "fail", null);
      return res;
    } catch (Exception e) {
      Response res = new Response(requestid, Response.ERROR, "fail", null);
      return res;
    }
  }
}