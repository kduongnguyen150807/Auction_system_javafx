package com.auction.server.controller;

import com.auction.server.service.RatingService;
import com.auction.shared.Rating;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.Set;

public class RatingRequestHandler implements RequestHandler {
    private final RatingService ratingService;

    public RatingRequestHandler(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Override
    public Set<String> supportedActions() {
        return Set.of(Request.SUBMIT_RATING, Request.GET_RATINGS);
    }

    @Override
    public Response handle(Request req, ClientHandler client) {
        String rid = req.getRequestId();
        String act = req.getAction();

        if (Request.SUBMIT_RATING.equals(act)) {
            Rating rating = (Rating) req.getPayload();
            return ratingService.submitRating(client.getCurrentUser(), rid, rating);
        }

        if (Request.GET_RATINGS.equals(act)) {
            int itemId = (int) req.getPayload();
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) ratingService.getRatingsByItem(itemId));
        }

        return new Response(rid, Response.ERROR, "unknown_action", null);
    }
}