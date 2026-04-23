package com.auction.server.controller;

import com.auction.server.service.AuctionManager;
import com.auction.server.service.BidQueryService;
import com.auction.shared.BidTransaction;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.Set;

public class BidRequestHandler implements RequestHandler {
    private final AuctionManager auctionManager;
    private final BidQueryService bidQueryService;

    public BidRequestHandler(AuctionManager auctionManager, BidQueryService bidQueryService) {
        this.auctionManager = auctionManager;
        this.bidQueryService = bidQueryService;
    }

    @Override
    public Set<String> supportedActions() {
        return Set.of(Request.BID, "get_bid_history");
    }

    @Override
    public Response handle(Request req, ClientHandler client) {
        String rid = req.getRequestId();

        if (Request.BID.equals(req.getAction())) {
            BidTransaction bid = (BidTransaction) req.getPayload();
            Response raw = auctionManager.processBid(bid);
            return new Response(rid, raw.getStatus(), raw.getMessage(), raw.getPayload());
        }

        if ("get_bid_history".equals(req.getAction())) {
            int itemId = (int) req.getPayload();
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) bidQueryService.getBidHistory(itemId));
        }

        return new Response(rid, Response.ERROR, "unknown_action", null);
    }
}