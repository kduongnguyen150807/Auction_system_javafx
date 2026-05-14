package com.auction.server.handler.user;

import com.auction.server.dao.user.WatchlistDao;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;
import java.util.Map;

public class WatchlistHandler implements ActionHandler {
    private final WatchlistDao watchlistDao = new WatchlistDao();

    @Override
    public Response handle(Request request, HandlerContext context) {
        int userId = context.getCurrentUser().getId();

        if (Request.GET_WATCHLIST.equals(request.getAction())) {
            List<Integer> watchedIds = watchlistDao.getUserWatchlist(userId);
            return new Response(request.getRequestId(), Response.OK, "success", (java.io.Serializable) watchedIds);
        }

        if (Request.TOGGLE_WATCHLIST.equals(request.getAction())) {
            Map<String, Object> payload = (Map<String, Object>) request.getPayload();
            int itemId = ((Number) payload.get("itemId")).intValue();
            boolean isWatching = (Boolean) payload.get("isWatching");

            watchlistDao.toggleWatchlist(userId, itemId, isWatching);
            return new Response(request.getRequestId(), Response.OK, "success", null);
        }

        return new Response(request.getRequestId(), Response.ERROR, "unknown", null);
    }
}