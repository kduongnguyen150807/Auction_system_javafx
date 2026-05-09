package com.auction.server.service;

import com.auction.server.dao.ItemDao;
import com.auction.server.dao.RatingDao;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Rating;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RatingService {
    private static final Logger LOGGER = Logger.getLogger(RatingService.class.getName());

    private final ItemDao itemDao;
    private final RatingDao ratingDao;

    public RatingService() {
        this(new ItemDao(), new RatingDao());
    }

    public RatingService(ItemDao itemDao, RatingDao ratingDao) {
        this.itemDao = itemDao;
        this.ratingDao = ratingDao;
    }

    public Response submitRating(User currentUser, String requestId, Rating rating) {
        try {
            Response validationError = validateRatingRequest(currentUser, requestId, rating);
            if (validationError != null) {
                return validationError;
            }

            Item item = itemDao.getById(rating.getItemId());
            int raterId = currentUser.getId();

            rating.setRaterUserId(raterId);
            rating.setRatedUserId(resolveRatedUserId(item, raterId));

            boolean inserted = ratingDao.insertRating(rating);
            if (!inserted) {
                return new Response(requestId, Response.ERROR, "fail", null);
            }

            ratingDao.recalcUserRating(rating.getRatedUserId());
            return new Response(requestId, Response.OK, "success", null);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Failed to submit rating", e);
            return new Response(requestId, Response.ERROR, "fail", null);
        }
    }

    private Response validateRatingRequest(User currentUser, String requestId, Rating rating) {
        if (currentUser == null) {
            return new Response(requestId, Response.ERROR, "not_logged_in", null);
        }

        if (rating == null) {
            return new Response(requestId, Response.ERROR, "invalid_payload", null);
        }

        Item item = itemDao.getById(rating.getItemId());
        if (item == null) {
            return new Response(requestId, Response.ERROR, "item_not_found", null);
        }

        if (!isAuctionEnded(item)) {
            return new Response(requestId, Response.ERROR, "auction_not_ended", null);
        }

        int raterId = currentUser.getId();
        if (!isParticipant(item, raterId)) {
            return new Response(requestId, Response.ERROR, "not_participant", null);
        }

        if (ratingDao.hasRated(rating.getItemId(), raterId)) {
            return new Response(requestId, Response.ERROR, "already_rated", null);
        }

        return null;
    }

    private boolean isAuctionEnded(Item item) {
        return item.getStatus() == ItemStatus.CLOSED || item.getStatus() == ItemStatus.FINISHED;
    }

    private boolean isParticipant(Item item, int userId) {
        return userId == item.getWinnerId() || userId == item.getSellerId();
    }

    private int resolveRatedUserId(Item item, int raterId) {
        return raterId == item.getWinnerId() ? item.getSellerId() : item.getWinnerId();
    }

    public List<Rating> getRatingsByItem(int itemId) {
        return ratingDao.getByItemId(itemId);
    }
}