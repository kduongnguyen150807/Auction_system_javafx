package com.auction.server.handler.misc;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.LeaderboardEntry;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;

public class LeaderboardHandler implements ActionHandler {
    @Override
    public Response handle(Request request, HandlerContext context) {
        List<LeaderboardEntry> ans = context.getAuctionManager().getLeaderboardservice().gettop(10);
        Response res = new Response(request.getRequestId(), Response.OK, "success", (java.io.Serializable) ans);
        return res;
    }
}