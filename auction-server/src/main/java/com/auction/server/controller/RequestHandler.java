package com.auction.server.controller;

import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.Set;

public interface RequestHandler {
    Set<String> supportedActions();
    Response handle(Request req, ClientHandler client);
}