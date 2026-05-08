package com.auction.server.dao.rating;

import com.auction.shared.Rating;
import java.util.List;

public interface RatingRepository {
  boolean insertRating(Rating rating);
  boolean hasRated(int itemId, int userId);
  List<Rating> getByItemId(int itemId);
  void recalcUserRating(int userId);
}
