package com.auction.server.service;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.transaction.TransactionManager;
import com.auction.server.domain.AuctionDomain;
import com.auction.server.domain.BidException;
import com.auction.server.domain.TaskValidate;
import com.auction.server.repository.ItemRepository;
import com.auction.server.repository.LotRepository;
import com.auction.server.repository.UserRepository;
import com.auction.server.store.AuctionStore;
import com.auction.server.store.AuctionTaskType;
import com.auction.server.store.AuctionTask;
import com.auction.shared.BidResult;
import com.auction.shared.ResultBase;
import com.auction.shared.dto.BidForm;
import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;
import com.auction.shared.linkv2.Response;
import com.auction.shared.user.User;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class AuctionService {
  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final LotRepository lotRepository;

  private final AuctionDomain auctionDomain =  new AuctionDomain();

  public AuctionService(ItemRepository itemRepository, UserRepository userRepository, LotRepository lotRepository) {
    this.itemRepository = itemRepository;
    this.userRepository = userRepository;
    this.lotRepository = lotRepository;
  }

  public Response<List<Item>> getAllItems(String requestId, HandlerContext handlerContext) {
    List<Item> items = TransactionManager.read(connection -> {
      return itemRepository.getAllItems(connection);
    });
    if (items.isEmpty()) {
      return Response.error(requestId, "No items found");
    } else {
      return Response.success(requestId, "Items found", items);
    }
  }

  public Response<Boolean> approveItem(String requestId, HandlerContext handlerContext, Item item) {
    if (item.getStatus() != ItemStatus.PENDING) {
      return Response.error(requestId, "item is not pending");
    }

    boolean success = TransactionManager.execute(conn -> {
      return itemRepository.approveItem(item.getId(), conn);
    });

    if (success) {
      return Response.success(requestId, null);
    } else {
      return Response.error(requestId, "item is not approved");
    }
  }

  public Response<Object> registerLot(String requestId, Item item, int sellerId) {
    boolean success = TransactionManager.execute(conn -> {
      return lotRepository.registerLot(item, sellerId, conn);
    });
    if (success) {
      return Response.success(requestId, "success", null);
    } else {
      return Response.error(requestId, "fail");
    }
  }

  public CompletableFuture<Response<BidResult>> submitBid(String requestId, BidForm bidForm, HandlerContext handlerContext) {
    /* validate bidForm */
    boolean valRes = TaskValidate.validate(bidForm);
    if (!valRes) {
      return CompletableFuture.completedFuture(Response.error(requestId, "invalid bid"));
    }

    return submitTask(requestId,
      bidForm.getItemId(),
      AuctionTaskType.BID,
      () -> {
      return placeBid(bidForm);
    });
  }

  public BidResult placeBid(BidForm bidForm) {
    int itemId = bidForm.getItemId();
    int bidderId = bidForm.getBidderId();
    double bidAmount = bidForm.getBidAmount();

    try {
      return TransactionManager.execute(conn -> {
        /* validate */
        Item item = itemRepository.getItemById(itemId, conn);
        if (item == null) {
          return BidResult.failure(
            "item not found",
            BidResult.NON_EXISTING_PRICE,
            BidResult.NON_EXISTING_WINNER
          );
        }
        User bidder = userRepository.findById(bidderId, conn);

        /* update for oldWinner */
        int oldWinnerId = item.getWinnerId();
        if (!(oldWinnerId == 0)) {
          userRepository.updateBalance(oldWinnerId, item.getCurrentPrice(), conn);
        }

        /* domain xu ly logic tinh toan */
        if (bidAmount < item.getMaxPrice()) {
          auctionDomain.executeNormalBid(item, bidderId, bidAmount, bidder.getBalance());
        } else {
          auctionDomain.executeBuyNowBid(item, bidderId, bidAmount, bidder.getBalance());
          itemRepository.updateStatus(itemId, ItemStatus.CLOSED, conn);
        }

        /* update for item */
        itemRepository.updateBidInfo(itemId, item.getCurrentPrice(), item.getWinnerId(), conn);
        userRepository.updateBalance(bidderId, -item.getCurrentPrice(), conn);
        return BidResult.success("place bid successfully", item.getCurrentPrice(), item.getWinnerId());
      });
    } catch (BidException e) {
      return BidResult.failure(
        e.getMessage(),
        BidResult.EXCEPTION_PRICE,
        BidResult.EXCEPTION_WINNER
      );
    } catch (Exception e) {
      return BidResult.failure(
        "fail to placeBid",
        BidResult.EXCEPTION_PRICE,
        BidResult.EXCEPTION_WINNER);
    }
  }

  private <T extends ResultBase> CompletableFuture<Response<T>> submitTask(String requestId, int itemId, AuctionTaskType taskType, Callable<T> action) {
    AuctionTask<T> task = new AuctionTask<>(taskType, action);
    AuctionStore.submit(itemId, task);

    return task.getFuture().thenApply(result -> {
      if (result.isSuccess()) {
        return Response.success(requestId, result.getMessage(), result);
      } else  {
        return Response.error(requestId, result.getMessage());
      }
    });
  }
}
