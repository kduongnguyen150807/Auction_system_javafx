package com.auction.server.handler.live;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;

public class GetLiveAuctionsHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    List<Item> items = context.getItemDao().getLiveOpenItems();
    return new Response(
        request.getRequestId(), Response.OK, "live_auctions", (java.io.Serializable) items);
  }
}
