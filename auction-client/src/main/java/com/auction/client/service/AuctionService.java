package com.auction.client.service;

import com.auction.client.store.AuctionStore;
import com.auction.client.ui.maindashboard.registerlot.LotForm;
import com.auction.client.ui.maindashboard.registerlot.LotMapper;
import com.auction.client.ui.utils.RequestHelper;
import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;
import com.auction.shared.linkv2.RequestType;
import com.auction.shared.linkv2.Response;
import com.auction.shared.linkv2.ResponseStatus;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AuctionService {
  private final static Logger LOGGER = LoggerFactory.getLogger(AuctionService.class);

  private static AuctionService instance;
  private final LotMapper lotMapper;
  private final AuctionStore auctionStore;

  private AuctionService() {
    this.auctionStore = new AuctionStore();
    lotMapper = new LotMapper();
  }



  public static AuctionService getInstance() {
    if (instance == null) {
      instance = new AuctionService();
    }
    return instance;
  }

  public void refreshItem() {
    RequestHelper.<List<Item>>sendRequest(RequestType.GET_ALL_ITEMS, null,
      response -> {
        List<Item> items = response.getData();
        auctionStore.refreshItem(items);
      },  response -> { response.printStackTrace(); });
  }

  public CompletableFuture<List<Item>> refreshItemv2() {
    CompletableFuture<List<Item>> future = new CompletableFuture<>();

    RequestHelper.<List<Item>>sendRequest(RequestType.GET_ALL_ITEMS, null,
      response -> {
        List<Item> items = response.getData();
        auctionStore.refreshItem(items);
        future.complete(items);
       },
        future::completeExceptionally
      );

    return future;
  }

  public ObservableList<Item> getItemsByStatus(ItemStatus status) {
    LOGGER.info("return items to controller by status {}", status);
    return auctionStore.filterStatus(status);
  }

  public ObservableList<Item> getAllItems() {
    return auctionStore.getItems();
  }

  public void registerLot(LotForm lotForm, Consumer<Response<Object>> callback, Consumer<Throwable> errorCallback) {
    RequestHelper.<Object>sendRequest(RequestType.REGISTER_LOT, lotMapper.map(lotForm),
      callback, errorCallback);
  }

}
