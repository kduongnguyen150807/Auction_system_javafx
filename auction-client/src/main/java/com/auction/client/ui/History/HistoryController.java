package com.auction.client.ui.History;

import com.auction.client.ClientSession;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.client.ui.TrangChu.HomeItemCardFactory;
import com.auction.shared.Item;
import java.util.concurrent.atomic.AtomicLong;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;

public class HistoryController {
  @FXML private FlowPane ongoingcontainer;
  @FXML private FlowPane upcomingcontainer;
  @FXML private FlowPane closedcontainer;
  @FXML private FlowPane pastcontainer;
  @FXML private FlowPane watchlistcontainer;

  private final HistoryPaneCards watchlistModel = new HistoryPaneCards();
  private final HistoryPaneCards trendingSectionCards = new HistoryPaneCards();
  private final HistoryPaneCards upcomingModel = new HistoryPaneCards();
  private final HistoryPaneCards closedModel = new HistoryPaneCards();
  private final HistoryPaneCards pastModel = new HistoryPaneCards();
  private final AtomicLong fetchGen = new AtomicLong(0);
  private final HistorySectionRenderer sectionRenderer = new HistorySectionRenderer(new HomeItemCardFactory());
  private final HistoryUpcomingCoordinator upcomingCoordinator = new HistoryUpcomingCoordinator();
  private Timeline sectionTimeline;

  @FXML
  public void initialize() {
    sectionTimeline =
        new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> tickLiveSections()));
    sectionTimeline.setCycleCount(Timeline.INDEFINITE);
    sectionTimeline.play();
    refreshHistory();
  }

  public void refreshHistory() {
    if (ClientSession.getCurrentUser() == null) {
      return;
    }
    int userId = ClientSession.getCurrentUser().getId();
    long currentGen = fetchGen.incrementAndGet();
    Thread thread =
        new Thread(
            () -> {
              HistoryDataLoader.PageData data = HistoryDataLoader.loadFullPage(userId);
              Platform.runLater(
                  () -> {
                    if (currentGen != fetchGen.get()) {
                      return;
                    }
                    applyPageData(data);
                  });
            });
    thread.setDaemon(true);
    thread.start();
  }

  public void updateWatchlistUi(int itemId, boolean isWatched) {
    updateCardHeart(trendingSectionCards, itemId, isWatched);
    updateCardHeart(upcomingModel, itemId, isWatched);
    updateCardHeart(closedModel, itemId, isWatched);
    updateCardHeart(pastModel, itemId, isWatched);
    updateCardHeart(watchlistModel, itemId, isWatched);
  }

  private void applyPageData(HistoryDataLoader.PageData data) {
    upcomingCoordinator.replaceCache(data.upcoming());
    syncSection(
        watchlistcontainer, data.watchlist(), watchlistModel, HistoryTimeCaptions::scheduled, false);
    syncSection(
        ongoingcontainer, data.trending(), trendingSectionCards, HistoryTimeCaptions::ongoing, true);
    syncSection(
        upcomingcontainer, data.upcoming(), upcomingModel, HistoryTimeCaptions::upcoming, false);
    syncSection(closedcontainer, data.closed(), closedModel, HistoryTimeCaptions::scheduled, false);
    syncSection(pastcontainer, data.past(), pastModel, HistoryTimeCaptions::scheduled, false);
  }

  private void tickLiveSections() {
    trendingSectionCards.cards.values().forEach(ItemCardController::updateTimeLabel);
    upcomingCoordinator.onTimelineTick(this::refreshUpcomingCaptions, this::refreshUpcomingAndTrending);
  }

  private void refreshUpcomingCaptions() {
    for (Item item : upcomingCoordinator.cachedUpcomingView()) {
      ItemCardController card = upcomingModel.cards.get(item.getId());
      if (card != null) {
        card.syncFromCatalogItemStaticTime(item, HistoryTimeCaptions.upcoming(item));
      }
    }
  }

  private void refreshUpcomingAndTrending() {
    if (ClientSession.getCurrentUser() == null) {
      return;
    }
    int userId = ClientSession.getCurrentUser().getId();
    upcomingCoordinator.refreshUpcomingAndTrendingAsync(
        userId,
        data ->
            Platform.runLater(
                () -> {
                  upcomingCoordinator.replaceCache(data.upcoming());
                  syncSection(
                      upcomingcontainer,
                      data.upcoming(),
                      upcomingModel,
                      HistoryTimeCaptions::upcoming,
                      false);
                  syncSection(
                      ongoingcontainer,
                      data.trending(),
                      trendingSectionCards,
                      HistoryTimeCaptions::ongoing,
                      true);
                }));
  }

  private void syncSection(
      FlowPane pane,
      java.util.List<Item> items,
      HistoryPaneCards model,
      java.util.function.Function<Item, String> captionFn,
      boolean liveTrendingCountdown) {
    if (pane == null || items == null) {
      return;
    }
    sectionRenderer.sync(pane, items, model, captionFn, liveTrendingCountdown);
  }

  private static void updateCardHeart(HistoryPaneCards model, int itemId, boolean isWatched) {
    ItemCardController card = model.cards.get(itemId);
    if (card != null) {
      card.setHeartUI(isWatched);
    }
  }
}
