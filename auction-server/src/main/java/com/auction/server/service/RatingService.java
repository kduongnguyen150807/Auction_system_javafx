package com.auction.server.service;

import com.auction.server.dao.ItemDao;
import com.auction.server.dao.RatingDao;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Rating;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;

public class RatingService {
    private final ItemDao itemDao;
    private final RatingDao ratingDao;

    public RatingService() {
        this.itemDao = new ItemDao();
        this.ratingDao = new RatingDao();
    }

    public Response submitRating(User currentUser, String requestId, Rating rating) {
        try {
            if (currentUser == null) {
                return new Response(requestId, Response.ERROR, "not_logged_in", null);
            }

            Item item = itemDao.getById(rating.getItemId());
            if (item == null) {
                return new Response(requestId, Response.ERROR, "item_not_found", null);
            }

            if (item.getStatus() != ItemStatus.CLOSED && item.getStatus() != ItemStatus.FINISHED) {
                return new Response(requestId, Response.ERROR, "auction_not_ended", null);
            }

            int raterId = currentUser.getId();
            if (raterId != item.getWinnerId() && raterId != item.getSellerId()) {
                return new Response(requestId, Response.ERROR, "not_participant", null);
            }

            if (ratingDao.hasRated(rating.getItemId(), raterId)) {
                return new Response(requestId, Response.ERROR, "already_rated", null);
            }

            rating.setRaterUserId(raterId);
            if (raterId == item.getWinnerId()) {
                rating.setRatedUserId(item.getSellerId());
            } else {
                rating.setRatedUserId(item.getWinnerId());
            }

            boolean ok = ratingDao.insertRating(rating);
            if (ok) {
                ratingDao.recalcUserRating(rating.getRatedUserId());
                return new Response(requestId, Response.OK, "success", null);
            }

            return new Response(requestId, Response.ERROR, "fail", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(requestId, Response.ERROR, "fail", null);
        }
    }

    public List<Rating> getRatingsByItem(int itemId) {
        return ratingDao.getByItemId(itemId);
    }
}