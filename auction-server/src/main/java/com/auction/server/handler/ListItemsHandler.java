package com.auction.server.handler;

import com.auction.shared.*;
import java.util.List;

public class ListItemsHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    List<Item> items = context.getItemDao().getAll();
    items.removeIf(item -> item.getStatus() != ItemStatus.OPEN);
    return new Response(request.getRequestId(), Response.OK, "success", (java.io.Serializable) items);
  }
}
