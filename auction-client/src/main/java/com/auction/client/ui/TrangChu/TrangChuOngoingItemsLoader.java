package com.auction.client.ui.TrangChu;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Fetches ongoing catalog items from the server on a background thread. */
final class TrangChuOngoingItemsLoader {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TrangChuOngoingItemsLoader.class);

  private TrangChuOngoingItemsLoader() {}

  static void loadAsync(Consumer<List<Item>> onItems) {
    Thread fetchThread =
        new Thread(
            () -> {
              try {
                int userId =
                    ClientSession.getCurrentUser() != null
                        ? ClientSession.getCurrentUser().getId()
                        : 0;
                Request request = new Request(Request.GET_ONGOING_BIDS, userId);
                Response response = NetworkClient.getInstance().sendRequestAndWait(request);
                if (response == null || !Response.OK.equals(response.getStatus())) {
                  return;
                }
                Object payload = response.getPayload();
                if (!(payload instanceof List<?> list)) {
                  return;
                }
                List<Item> items = new ArrayList<>();
                for (Object entry : list) {
                  if (entry instanceof Item item) {
                    items.add(item);
                  }
                }
                List<Item> copy = items;
                Platform.runLater(() -> onItems.accept(copy));
              } catch (Exception e) {
                LOGGER.warn("Failed to refresh auction items", e);
              }
            });
    fetchThread.setDaemon(true);
    fetchThread.start();
  }
}
