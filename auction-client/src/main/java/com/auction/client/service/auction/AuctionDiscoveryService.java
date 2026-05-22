package com.auction.client.service.auction;

import com.auction.client.store.lotsinformation.ClosedLots;
import com.auction.client.store.lotsinformation.OngoingLots;
import com.auction.client.store.userinformation.UserModel;
import com.auction.client.util.RequestHelper;
import com.auction.shared.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AuctionDiscoveryService {

  public CompletableFuture<List<Item>> refreshOngoingLots() {
    return RequestHelper.sendRequest(Request.GET_ONGOING_LOTS, null)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> ongoingLots) {
          List<Item> items = (List<Item>) ongoingLots;
          OngoingLots.AUCTION_STORE.loadOngoingItems(items);
          return items;
        }
        return List.of();
      });
  }

  public CompletableFuture<List<Item>> refreshClosedLots() {
    return RequestHelper.sendRequest(Request.GET_CLOSED_BIDS, null)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> closedLots) {
          List<Item> items = (List<Item>) closedLots;
          ClosedLots.CLOSED_LOTS.setClosedLots(items);
          return items;
        }
        return List.of();
      });
  }

  public CompletableFuture<List<Item>> getTrendingLots() {
    return RequestHelper.sendRequest(Request.GET_TRENDING_LOTS, null)
      .thenApply(response ->  {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> trendingLots) {
          return (List<Item>) trendingLots;
        }
        return List.of();
      });
  }

  public CompletableFuture<List<LeaderboardEntry>> getLeaderboard() {
    return RequestHelper.sendRequest(Request.GET_LEADERBOARD, null)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> osl) {
          return (List<LeaderboardEntry>) osl;
        }
        return List.of();
      });
  }

  public CompletableFuture<User> getUserById(int id) {
    return RequestHelper.sendRequest(Request.GET_USER_BY_ID, id)
      .thenApply(response ->  {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof User) {
          return (User) response.getPayload();
        }
        return null;
      });
  }

  public void fetchLeaderboardData(Consumer<List<UserModel>> consumer) {
    getLeaderboard().thenCompose(leaderboard -> {
      if (leaderboard == null || leaderboard.isEmpty()) {
        return CompletableFuture.completedFuture(new ArrayList<UserModel>());
      }

      int size = leaderboard.size();
      CompletableFuture<UserModel>[] futures = new CompletableFuture[size];

      for (int i = 0; i < size; i++) {
        final int rank = i + 1;
        int userId = leaderboard.get(i).getUserid();

        futures[i] = this.getUserById(userId)
          .thenApply(userDto -> {
            if (userDto == null) {
              return null;
            }
            UserModel model = new UserModel(userDto);
            model.setRank(rank);
            return model;
          });
      }

        return CompletableFuture.allOf(futures)
          .thenApply(v -> {
            List<UserModel> orderedModels = new ArrayList<>();
            for (CompletableFuture<UserModel> future : futures) {
              UserModel model = future.join();
              if (model != null) {
                orderedModels.add(model);
              }
            }
            return orderedModels;
          });
      })
      .thenAccept(finalUserModels -> {
        if (consumer != null) {
          System.out.println("[Service] Đã xử lý xong dữ liệu. Kích hoạt callback render.");
          consumer.accept(finalUserModels);
        }
      })
      .exceptionally(ex -> {
        System.err.println("[Service] Lỗi nghiêm trọng khi xử lý dữ liệu Leaderboard:");
        ex.printStackTrace();
        if (consumer != null) {
          consumer.accept(new ArrayList<>());
        }
        return null;
    });
  }
}