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
  }

  private void renderItemCards() { }
}
