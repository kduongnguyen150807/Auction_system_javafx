package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.Map;

public class SellerCancelItemHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    User me = context.getCurrentUser();
    int itemId = -1;
    try {
      @SuppressWarnings("unchecked")
      Map<String, String> data = (Map<String, String>) request.getPayload();
      if (data == null || data.get("itemid") == null) {
        return new Response(request.getRequestId(), Response.ERROR, "invalid_payload", null);
      }
      itemId = Integer.parseInt(data.get("itemid").trim());
      Item item = context.getItemDao().getById(itemId);
      if (item == null) {
        return new Response(request.getRequestId(), Response.ERROR, "not_found", null);
      }
      if (item.getSellerId() != me.getId()) {
        return new Response(request.getRequestId(), Response.ERROR, "forbidden", null);
      }
      if (item.getStatus() == ItemStatus.CANCELED) {
        return successWithItem(request.getRequestId(), context, itemId);
      }
      if (item.getStatus() == ItemStatus.PENDING) {
        boolean ok = context.getItemDao().sellerCancelPending(itemId, me.getId());
        if (!ok) {
          return reconcileCanceled(request.getRequestId(), context, itemId, me.getId());
        }
        return successWithItem(request.getRequestId(), context, itemId);
      }
      if (item.getStatus() == ItemStatus.OPEN) {
        boolean ok = context.getAuctionManager().voluntarySellerCancelOpenAuction(me.getId(), itemId);
        if (!ok) {
          return reconcileCanceled(request.getRequestId(), context, itemId, me.getId());
        }
        return successWithItem(request.getRequestId(), context, itemId);
      }
      return new Response(request.getRequestId(), Response.ERROR, "cannot_cancel_status", null);
    } catch (Exception e) {
      if (itemId > 0 && me != null) {
        return reconcileCanceled(request.getRequestId(), context, itemId, me.getId());
      }
      return new Response(request.getRequestId(), Response.ERROR, "fail", null);
    }
  }

  private static Response successWithItem(String requestId, HandlerContext context, int itemId) {
    Item updated = context.getItemDao().getById(itemId);
    return new Response(requestId, Response.OK, "success", updated);
  }

  /** Cancel may have committed before a post-commit error; treat as success when DB says CANCELED. */
  private static Response reconcileCanceled(
      String requestId, HandlerContext context, int itemId, int sellerId) {
    Item again = context.getItemDao().getById(itemId);
    if (again != null
        && again.getSellerId() == sellerId
        && again.getStatus() == ItemStatus.CANCELED) {
      return new Response(requestId, Response.OK, "success", again);
    }
    return new Response(requestId, Response.ERROR, "cannot_cancel", null);
  }
}
