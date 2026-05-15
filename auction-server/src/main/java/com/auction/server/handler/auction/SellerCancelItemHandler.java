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
      if (item.getStatus() == ItemStatus.PENDING) {
        boolean ok = context.getItemDao().sellerCancelPending(itemId, me.getId());
        return new Response(
            request.getRequestId(),
            ok ? Response.OK : Response.ERROR,
            ok ? "success" : "fail",
            null);
      }
      if (item.getStatus() == ItemStatus.OPEN) {
        boolean ok = context.getAuctionManager().voluntarySellerCancelOpenAuction(me.getId(), itemId);
        return new Response(
            request.getRequestId(),
            ok ? Response.OK : Response.ERROR,
            ok ? "success" : "cannot_cancel",
            null);
      }
      return new Response(request.getRequestId(), Response.ERROR, "cannot_cancel_status", null);
    } catch (Exception e) {
      return new Response(request.getRequestId(), Response.ERROR, "fail", null);
    }
  }
}
