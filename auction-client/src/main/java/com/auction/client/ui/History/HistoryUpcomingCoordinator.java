package com.auction.client.ui.History;

import com.auction.shared.Item;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Tracks cached upcoming lots and coordinates periodic refresh when auctions go live.
 */
final class HistoryUpcomingCoordinator {

  private final List<Item> cachedUpcoming = new ArrayList<>();
  private final AtomicBoolean refreshInFlight = new AtomicBoolean(false);

  List<Item> cachedUpcomingView() {
    return List.copyOf(cachedUpcoming);
  }

  void replaceCache(List<Item> upcoming) {
    cachedUpcoming.clear();
    if (upcoming != null) {
      cachedUpcoming.addAll(upcoming);
    }
  }

  void onTimelineTick(Runnable refreshCaptionsOnly, Runnable refreshUpcomingAndTrending) {
    if (cachedUpcoming.isEmpty()) {
      return;
    }
    LocalDateTime now = LocalDateTime.now();
    boolean anyStarted =
        cachedUpcoming.stream()
            .anyMatch(item -> item.getStartTime() != null && !item.getStartTime().isAfter(now));
    if (anyStarted) {
      refreshUpcomingAndTrending.run();
    } else {
      refreshCaptionsOnly.run();
    }
  }

  void refreshUpcomingAndTrendingAsync(int userId, Consumer<HistoryDataLoader.UpcomingTrendingData> onDone) {
    if (!refreshInFlight.compareAndSet(false, true)) {
      return;
    }
    Thread thread =
        new Thread(
            () -> {
              try {
                HistoryDataLoader.UpcomingTrendingData data =
                    HistoryDataLoader.loadUpcomingAndTrending(userId);
                onDone.accept(data);
              } finally {
                refreshInFlight.set(false);
              }
            });
    thread.setDaemon(true);
    thread.start();
  }
}
