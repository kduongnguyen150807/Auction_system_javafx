package com.auction.server.handler;

import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;

public class ItemQueryHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();

    switch (action) {
      case Request.GET_MY_ITEMS: {
        int sellerId = (int) request.getPayload();
        List<Item> items = context.getItemDao().getBySellerId(sellerId);
        return new Response(requestId, Response.OK, "success", (java.io.Serializable) items);
      }

      case Request.GET_ITEM_BY_ID: {
        int itemId = (int) request.getPayload();
        Item item = context.getItemDao().getById(itemId);
        return new Response(requestId,
            item != null ? Response.OK : Response.ERROR,
            item != null ? "success" : "not_found", item);
      }

      case Request.GET_PENDING_ITEMS: {
        List<Item> items = context.getItemDao().getPendingItems();
        return new Response(requestId, Response.OK, "success", (java.io.Serializable) items);
      }

      case Request.APPROVE_ITEM: {
        int itemId = (int) request.getPayload();
        boolean success = context.getItemDao().approveItem(itemId);
        return new Response(requestId,
            success ? Response.OK : Response.ERROR,
            success ? "success" : "fail", null);
      }

      case Request.REJECT_ITEM: {
        int itemId = (int) request.getPayload();
        boolean success = context.getItemDao().rejectItem(itemId);
        return new Response(requestId,
            success ? Response.OK : Response.ERROR,
            success ? "success" : "fail", null);
      }

      default:
        return new Response(requestId, Response.ERROR, "unknown_action", null);
    }
  }
}
