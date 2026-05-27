package com.auction.server.handler.auth;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectHandler implements ActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReconnectHandler.class);

    @Override
    public Response handle(Request request, HandlerContext context) {
        try {
            String token = (String) request.getPayload();

            if (token == null || token.trim().isEmpty()) {
                return new Response(request.getRequestId(), Response.ERROR, "invalid_session", null);
            }

            User user = context.getAuctionManager().getsession(token.trim());

            if (user != null) {
                context.setCurrentUser(user);
                return new Response(request.getRequestId(), Response.OK, "reconnected", user);
            }

            return new Response(request.getRequestId(), Response.ERROR, "invalid_or_expired_session", null);
        } catch (ClassCastException e) {
            LOGGER.warn("Reconnect payload cast failed: {}", e.getMessage());
            return new Response(request.getRequestId(), Response.ERROR, "invalid_payload", null);
        } catch (Exception e) {
            LOGGER.error("Unexpected reconnect error", e);
            return new Response(request.getRequestId(), Response.ERROR, "server_error", null);
        }
    }
}