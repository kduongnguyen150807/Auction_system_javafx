package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.auction.TrieManager;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;

public class AutocompleteHandler implements ActionHandler {
    @Override
    public Response handle(Request request, HandlerContext context) {
        // REFACTOR: validate prefix length before searching to optimize performance
        String prefix = (String) request.getPayload();
        List<String> suggestions = TrieManager.getInstance().search(prefix);
        Response ans = new Response(request.getRequestId(), Response.OK, "success", (java.io.Serializable) suggestions);
        return ans;
    }
}