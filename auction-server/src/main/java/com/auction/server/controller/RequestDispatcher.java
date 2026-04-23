package com.auction.server.controller;

import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestDispatcher {
    private final Map<String, RequestHandler> handlers = new HashMap<>();

    public RequestDispatcher(List<RequestHandler> handlerList) {
        for (RequestHandler h : handlerList) {
            for (String action : h.supportedActions()) {
                handlers.put(action, h);
            }
        }
    }

    public Response dispatch(Request req, ClientHandler client) {
        RequestHandler handler = handlers.get(req.getAction());
        if (handler == null) {
            return new Response(req.getRequestId(), Response.ERROR, "unknown_action", null);
        }
        return handler.handle(req, client);
    }
}