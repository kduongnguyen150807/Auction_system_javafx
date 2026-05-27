package com.auction.client.ui.History;

import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.ArrayList;
import java.util.List;

/** Fetches history-page item lists from the server. */
final class HistoryDataLoader {

  record PageData(
      List<Item> watchlist,
      List<Item> trending,
      List<Item> upcoming,
      List<Item> closed,
      List<Item> past) {}

  record UpcomingTrendingData(List<Item> upcoming, List<Item> trending) {}

  private HistoryDataLoader() {}

  static PageData loadFullPage(int userId) {
    return new PageData(
        fetchList(Request.GET_WATCHLIST_ITEMS, userId),
        fetchTrendingForCatalogKind(),
        fetchList(Request.GET_UPCOMING_BIDS, userId),
        fetchList(Request.GET_CLOSED_BIDS, userId),
        fetchList(Request.GET_PAST_BIDS, userId));
  }

  static UpcomingTrendingData loadUpcomingAndTrending(int userId) {
    return new UpcomingTrendingData(
        fetchList(Request.GET_UPCOMING_BIDS, userId), fetchTrendingForCatalogKind());
  }

  @SuppressWarnings("unchecked")
  private static List<Item> fetchList(String action, int userId) {
    Request req = new Request(action, userId);
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && Response.OK.equals(res.getStatus()) && res.getPayload() instanceof List<?> raw) {
      List<Item> items = new ArrayList<>();
      for (Object obj : raw) {
        if (obj instanceof Item item) {
          items.add(item);
        }
      }
      return items;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  static List<Item> fetchTrendingForCatalogKind() {
    AuctionType kind = KhungController.getCatalogAuctionType();
    if (kind == null) {
      kind = AuctionType.ENGLISH;
    }
    Request req = new Request(Request.GET_TRENDING_LOTS, kind.dbName());
    Response res = NetworkClient.getInstance().sendRequestAndWait(req);
    if (res != null && Response.OK.equals(res.getStatus()) && res.getPayload() instanceof List<?> raw) {
      List<Item> items = new ArrayList<>();
      for (Object obj : raw) {
        if (obj instanceof Item item) {
          items.add(item);
        }
      }
      return items;
    }
    return new ArrayList<>();
  }
}
