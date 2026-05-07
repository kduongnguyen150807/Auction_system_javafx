package com.auction.client.ui.maindashboard;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.component.ItemCard;
import com.auction.shared.Lot;
import com.auction.shared.link.Request;
import com.auction.shared.link.RequestType;
import com.auction.shared.link.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class AuctionController implements CanRefresh {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuctionController.class);

  private final List<Lot> cachedLot = new ArrayList<>();

  @FXML
  private HBox trendingBind;

  @FXML
  public void initialize() {
    refreshData();
    renderItemCards();

  }

  @Override
  public void refreshData() {
    Thread fetchThread = new Thread(() -> {
      try {
        Request request = new Request(RequestType.GET_ONGOING_BIDS, null);
        Response response = NetworkClient.getInstance().sendRequestAndWait(request);
        LOGGER.info("Response from AuctionController: {}", response.getMessage());

        if (response == null || !Response.OK.equals(response.getStatus())) return;
        Object data = response.getPayload();
        if (!(data instanceof List<?> list)) return;
        List<Lot> lots = (List<Lot>) list;
        for(Lot lot: lots) {
          cachedLot.add(lot);
        }
      } catch (Exception e) {
        LOGGER.error("Error while fetching ongoing bids", e);
      }
    });
    fetchThread.setDaemon(true);
    fetchThread.start();
  }

  private void renderItemCards() {
    LOGGER.info("cached lot size: {}", cachedLot.size());
    for (Lot lot : cachedLot) {
      ItemCard itemCard = new ItemCard();
      trendingBind.getChildren().add(itemCard);
      LOGGER.info("Item card: {}", lot.toString());
    }
  }
}
