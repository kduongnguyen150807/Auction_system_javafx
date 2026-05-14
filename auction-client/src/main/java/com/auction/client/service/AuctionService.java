package com.auction.client.service;

import com.auction.client.repository.AuctionRepository;
import com.auction.client.store.AuctionStore;
import com.auction.client.ui.maindashboard.registerlot.LotForm;
import com.auction.client.ui.maindashboard.registerlot.LotMapper;
import com.auction.client.ui.utils.RequestHelper;
import com.auction.shared.dto.BidForm;
import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;
import com.auction.shared.linkv2.RequestType;
import com.auction.shared.linkv2.Response;
import com.auction.shared.linkv2.ResponseStatus;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AuctionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuctionService.class);

  private final AuctionStore auctionStore = new AuctionStore();
  private final AuctionRepository auctionRepository = new AuctionRepository();
  private final LotMapper lotMapper = new LotMapper();

  private static AuctionService instance;

  private AuctionService() {}

  public static AuctionService getInstance() {
    if (instance == null) {
      instance = new AuctionService();
    }
    return instance;
  }

  public CompletableFuture<ResponseStatus> registerLot(LotForm form) {
    Item item = lotMapper.map(form);
    return auctionRepository.registerLot(item);
  }

  public CompletableFuture<List<Item>> refreshItems() {
    return auctionRepository.getAllItems()
      .thenApply(items -> {
        auctionStore.refreshItem(items);
        return items;
      });
  }

  public CompletableFuture<ResponseStatus> placeBid(BidForm form) {
    return auctionRepository.placeBid(form)
      .thenApply(status ->  {
        if (status == ResponseStatus.SUCCESS) {
          LOGGER.info("Successfully placed lot for auction");
        } else  {
          LOGGER.info("Failed to place lot for auction");
        }
        return status;
      });
  }

  public ObservableList<Item> getItemsByStatus(ItemStatus status) {
    return auctionStore.filterStatus(status);
  }

  public void setFocusedItem(Item item) {
    auctionStore.setFocusedItem(item);
  }

  public Item getFocusedItem() {
    return auctionStore.getFocusedItem();
  }

  public ObjectProperty<Item> getFocusedItemProperty() { return auctionStore.focusedItemProperty(); }
}
