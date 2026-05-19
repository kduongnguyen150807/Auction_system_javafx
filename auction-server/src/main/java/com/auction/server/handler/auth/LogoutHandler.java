package com.auction.server.handler.auth;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

public class LogoutHandler implements ActionHandler {

    @Override
    public Response handle(
            Request request,
            HandlerContext context) {

        User currentUser = context.getCurrentUser();

        if (currentUser == null) {
            return new Response(
                    request.getRequestId(),
                    Response.ERROR,
                    "not_logged_in",
                    null);
        }

        boolean ok =
                context
                        .getUserService()
                        .clearSessionToken(currentUser.getId());

        if (!ok) {
            return new Response(
                    request.getRequestId(),
                    Response.ERROR,
                    "logout_failed",
                    null);
        }

        context.setCurrentUser(null);

        return new Response(
                request.getRequestId(),
                Response.SUCCESS,
                "logout_success",
                null);
    }
}