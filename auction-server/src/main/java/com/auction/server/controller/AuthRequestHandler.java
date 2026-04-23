package com.auction.server.controller;

import com.auction.server.service.UserService;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.Map;
import java.util.Set;

public class AuthRequestHandler implements RequestHandler {
    private final UserService userService;

    public AuthRequestHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Set<String> supportedActions() {
        return Set.of(Request.LOGIN, Request.SIGNUP);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response handle(Request req, ClientHandler client) {
        String rid = req.getRequestId();

        if (Request.LOGIN.equals(req.getAction())) {
            Map<String, String> data = (Map<String, String>) req.getPayload();
            User u = userService.login(data.get("username"), data.get("password"));
            if (u != null) {
                client.setCurrentUser(u);
                return new Response(rid, Response.OK, "success", u);
            }
            return new Response(rid, Response.ERROR, "fail", null);
        }

        if (Request.SIGNUP.equals(req.getAction())) {
            User u = (User) req.getPayload();
            boolean ok = userService.signup(u);
            return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "success" : "duplicate", null);
        }

        return new Response(rid, Response.ERROR, "unknown_action", null);
    }
}