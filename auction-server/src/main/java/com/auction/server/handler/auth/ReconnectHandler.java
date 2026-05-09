package com.auction.server.handler.auth;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

public class ReconnectHandler implements ActionHandler {
    @Override
    public Response handle(Request request, HandlerContext context) {
        // REFACTOR: add token expiration logic to prevent indefinite session reuse
        String token = (String) request.getPayload();
        User user = context.getAuctionManager().getsession(token);
        if (user != null) {
            context.setCurrentUser(user);
            Response ans = new Response(request.getRequestId(), Response.OK, "reconnected", user);
            return ans;
        }
        Response res = new Response(request.getRequestId(), Response.ERROR, "invalid_session", null);
        return res;
    }
}