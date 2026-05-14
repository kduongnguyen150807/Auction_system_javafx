package com.auction.client.ui.TrangChu;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Fetches ongoing catalog + trending top lots from the server on a background thread. */
final class TrangChuOngoingItemsLoader {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TrangChuOngoingItemsLoader.class);

  private TrangChuOngoingItemsLoader() {}

  static void loadAsync(Consumer<TrangChuCatalogLoadResult> onDone) {
    Thread fetchThread =
        new Thread(
            () -> {
              try {
                NetworkClient nc = NetworkClient.getInstance();

                int userId =
                    ClientSession.getCurrentUser() != null
                        ? ClientSession.getCurrentUser().getId()
                        : 0;
                Response ongoingRes =
                    nc.sendRequestAndWait(new Request(Request.GET_ONGOING_BIDS, userId));

                AuctionType catalogKind =
                    KhungController.getCatalogAuctionType() != null
                        ? KhungController.getCatalogAuctionType()
                        : AuctionType.ENGLISH;
                Response trendingRes =
                    nc.sendRequestAndWait(
                        new Request(Request.GET_TRENDING_LOTS, catalogKind.dbName()));

                List<Item> ongoing = parseItemListPayload(ongoingRes);
                List<Item> trending = parseItemListPayload(trendingRes);

                List<Item> ongoingCopy = new ArrayList<>(ongoing);
                List<Item> trendingCopy = new ArrayList<>(trending);
                Platform.runLater(
                    () ->
                        onDone.accept(
                            new TrangChuCatalogLoadResult(ongoingCopy, trendingCopy)));
              } catch (Exception e) {
                LOGGER.warn("Failed to refresh auction catalog / trending", e);
              }
            });
    fetchThread.setDaemon(true);
    fetchThread.start();
  }

  private static List<Item> parseItemListPayload(Response response) {
    if (response == null || !Response.OK.equals(response.getStatus())) {
      return new ArrayList<>();
    }
    Object payload = response.getPayload();
    if (!(payload instanceof List<?> list)) {
      return new ArrayList<>();
    }
    List<Item> items = new ArrayList<>();
    for (Object entry : list) {
      if (entry instanceof Item item) {
        items.add(item);
      }
    }
    return items;
  }
}
