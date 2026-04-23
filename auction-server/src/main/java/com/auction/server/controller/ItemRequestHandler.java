package com.auction.server.controller;

import com.auction.server.service.ItemQueryService;
import com.auction.server.service.ItemService;
import com.auction.server.service.LotService;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.Map;
import java.util.Set;

public class ItemRequestHandler implements RequestHandler {
    private final ItemService itemService;
    private final ItemQueryService itemQueryService;
    private final LotService lotService;

    public ItemRequestHandler(
            ItemService itemService,
            ItemQueryService itemQueryService,
            LotService lotService) {
        this.itemService = itemService;
        this.itemQueryService = itemQueryService;
        this.lotService = lotService;
    }

    @Override
    public Set<String> supportedActions() {
        return Set.of(
                Request.LIST,
                Request.GET_ONGOING_LOTS,
                Request.ADD_LOT,
                Request.GET_ITEM_BY_ID,
                "get_my_items",
                Request.GET_ONGOING_BIDS,
                Request.GET_UPCOMING_BIDS,
                "getclosedbids",
                "getpastbids");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response handle(Request req, ClientHandler client) {
        String rid = req.getRequestId();
        String act = req.getAction();
        Object pay = req.getPayload();

        if (Request.LIST.equals(act) || Request.GET_ONGOING_LOTS.equals(act)) {
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) itemQueryService.getOpenItems());
        }

        if (Request.ADD_LOT.equals(act)) {
            boolean ok = itemService.addLot((Map<String, String>) pay);
            return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "success" : "fail", null);
        }

        if (Request.GET_ITEM_BY_ID.equals(act)) {
            var item = itemQueryService.getById((int) pay);
            return new Response(rid, item != null ? Response.OK : Response.ERROR,
                    item != null ? "success" : "not_found", item);
        }

        if ("get_my_items".equals(act)) {
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) itemQueryService.getBySellerId((int) pay));
        }

        if (Request.GET_ONGOING_BIDS.equals(act)) {
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) lotService.getOngoingBids((int) pay));
        }

        if (Request.GET_UPCOMING_BIDS.equals(act)) {
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) lotService.getUpcomingBids((int) pay));
        }

        if ("getclosedbids".equals(act)) {
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) lotService.getClosedBids((int) pay));
        }

        if ("getpastbids".equals(act)) {
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) lotService.getPastBids((int) pay));
        }

        return new Response(rid, Response.ERROR, "unknown_action", null);
    }
}