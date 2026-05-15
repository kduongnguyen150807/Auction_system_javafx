package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.auction.DutchAuctionCatalogSync;
import com.auction.server.service.auction.SettlementService;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;

public class ItemQueryHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestid = request.getRequestId();

    switch (action) {
      case Request.GET_MY_ITEMS:
        int sellerid = (int) request.getPayload();
        List<Item> items = context.getItemDao().getBySellerId(sellerid);
        Response res1 = new Response(requestid, Response.OK, "success", (java.io.Serializable) items);
        return res1;

      case Request.GET_ITEM_BY_ID:
        int itemid = (int) request.getPayload();
        Item item = context.getItemDao().getById(itemid);
        if (item != null) {
          DutchAuctionCatalogSync.syncItem(context.getItemDao(), item);
        }
        Response res2 = new Response(
                requestid,
                item != null ? Response.OK : Response.ERROR,
                item != null ? "success" : "not_found",
                item);
        return res2;

      case Request.GET_PENDING_ITEMS:
        List<Item> pendingitems = context.getItemDao().getPendingItems();
        Response res3 = new Response(requestid, Response.OK, "success", (java.io.Serializable) pendingitems);
        return res3;

      case Request.APPROVE_ITEM:
        int approveid = (int) request.getPayload();
        boolean ans1 = context.getItemDao().approveItem(approveid);
        if (ans1) {
          Item res = context.getItemDao().getById(approveid);
          if (res != null) {
            if (res.getEndTime() != null) {
              SettlementService.getInstance().schedule(approveid, res.getEndTime());
            }
            // TÍNH NĂNG 5: CẬP NHẬT CÂY TRIE NGAY KHI ADMIN DUYỆT SẢN PHẨM
            com.auction.server.service.auction.TrieManager.getInstance().insertNewItem(res.getName());
          }
        }
        Response res4 = new Response(
                requestid,
                ans1 ? Response.OK : Response.ERROR,
                ans1 ? "success" : "fail",
                null);
        return res4;

      case Request.REJECT_ITEM:
        int rejectid = (int) request.getPayload();
        boolean ans2 = context.getItemDao().rejectItem(rejectid);
        Response res5 = new Response(
                requestid,
                ans2 ? Response.OK : Response.ERROR,
                ans2 ? "success" : "fail",
                null);
        return res5;

      default:
        Response res6 = new Response(requestid, Response.ERROR, "unknown_action", null);
        return res6;
    }
  }
}