package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;

public class ListItemsHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    List<Item> items = context.getItemDao().getOpenItems();

    return new Response(
            request.getRequestId(),
            Response.OK,
            "success",
            (java.io.Serializable) items);
  }
}