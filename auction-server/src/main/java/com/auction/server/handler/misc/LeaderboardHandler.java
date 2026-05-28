package com.auction.server.handler.misc;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.LeaderboardEntry;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderboardHandler implements ActionHandler {
    private static final Logger LOGGER = Logger.getLogger(LeaderboardHandler.class.getName());
    private static final int DEFAULT_TOP_LIMIT = 10;

    @Override
    public Response handle(Request request, HandlerContext context) {
        String requestId = request != null ? request.getRequestId() : null;

        try {
            if (request == null) {
                LOGGER.warning("Received null request in LeaderboardHandler");
                return new Response(null, Response.ERROR, "null_request", null);
            }

            if (context == null || context.getAuctionManager() == null) {
                LOGGER.warning(() -> "Missing context or auction manager, requestId=" + requestId);
                return new Response(requestId, Response.ERROR, "server_context_error", null);
            }

            LOGGER.info(() -> "Loading leaderboard, requestId=" + requestId);

            List<LeaderboardEntry> entries = context
                    .getAuctionManager()
                    .getLeaderboardservice()
                    .gettop(DEFAULT_TOP_LIMIT);

            if (entries == null) {
                entries = Collections.emptyList();
            }

            return new Response(requestId, Response.OK, "success", (Serializable) entries);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Server error while loading leaderboard, requestId=" + requestId, e);
            return new Response(requestId, Response.ERROR, "server_error", null);
        }
    }
}