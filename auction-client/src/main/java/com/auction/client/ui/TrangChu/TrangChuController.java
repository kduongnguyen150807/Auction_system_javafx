package com.auction.client.ui.TrangChu;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML controller for the auction home: orchestrates data refresh, filters, trending list, category
 * carousels, and live timers. Domain logic lives in {@link AuctionFilterContext}, {@link
 * CatalogRowSynchronizer}, and {@link CategoryCarouselSupport}.
 */
public class TrangChuController {
  private static final Logger LOGGER = LoggerFactory.getLogger(TrangChuController.class);
  private static final int AUTO_REFRESH_SECONDS = 30;

  private static TrangChuController instance;

  private final CatalogRowSynchronizer rowSynchronizer =
      new CatalogRowSynchronizer(new HomeItemCardFactory());

  @FXML private VBox TrendingBind;
  @FXML private HBox categoryArtRow;
  @FXML private HBox categoryElectronicsRow;
  @FXML private HBox categoryVehiclesRow;
  @FXML private Button categoryArtPrev;
  @FXML private Button categoryArtNext;
  @FXML private Button categoryElectronicsPrev;
  @FXML private Button categoryElectronicsNext;
  @FXML private Button categoryVehiclesPrev;
  @FXML private Button categoryVehiclesNext;

  @FXML private ToggleButton tabEnglishAuctions;
  @FXML private ToggleButton tabDutchAuctions;

  private HBox[] categoryRows;
  private Button[] categoryPrevButtons;
  private Button[] categoryNextButtons;
  private final int[] categoryCarouselOffset =
      new int[TrangChuCatalogConstants.SLOT_CATEGORIES.length];

  private final List<Item> cachedItems = new ArrayList<>();
  private final Map<Integer, ItemCardController> trendingCardMap = new HashMap<>();
  private final Map<Integer, Node> trendingRootByItemId = new HashMap<>();

  private final List<Map<Integer, ItemCardController>> categoryCardMaps = new ArrayList<>();
  private final List<Map<Integer, Node>> categoryRootMaps = new ArrayList<>();

  private String keyword = "";
  private String category = "All";
  private Timeline countdownTimeline;
  private Timeline autoRefreshTimeline;

  private ToggleGroup catalogAuctionKindGroup;

  public TrangChuController() {
    for (int i = 0; i < TrangChuCatalogConstants.SLOT_CATEGORIES.length; i++) {
      categoryCardMaps.add(new HashMap<>());
      categoryRootMaps.add(new HashMap<>());
    }
  }

  @FXML
  void initialize() {
    if (instance != null) {
      instance.stopTimelines();
    }
    instance = this;
    categoryRows = new HBox[] {categoryArtRow, categoryElectronicsRow, categoryVehiclesRow};
    categoryPrevButtons =
        new Button[] {categoryArtPrev, categoryElectronicsPrev, categoryVehiclesPrev};
    categoryNextButtons =
        new Button[] {categoryArtNext, categoryElectronicsNext, categoryVehiclesNext};
    if (TrendingBind != null) {
      TrendingBind.setMaxWidth(Double.MAX_VALUE);
    }
    initCatalogAuctionTabs();
    setFilters(KhungController.getSearchKeyword(), KhungController.getCategoryFilter());
    refreshItems();
    startTimelines();
  }

  private void initCatalogAuctionTabs() {
    if (tabEnglishAuctions == null || tabDutchAuctions == null) {
      return;
    }
    catalogAuctionKindGroup = new ToggleGroup();
    tabEnglishAuctions.setToggleGroup(catalogAuctionKindGroup);
    tabDutchAuctions.setToggleGroup(catalogAuctionKindGroup);
    catalogAuctionKindGroup
        .selectedToggleProperty()
        .addListener(
            (obs, oldT, newT) -> {
              if (newT == null) {
                return;
              }
              AuctionType nu =
                  newT == tabDutchAuctions ? AuctionType.DUTCH : AuctionType.ENGLISH;
              if (KhungController.getCatalogAuctionType() != nu) {
                KhungController.setCatalogAuctionType(nu);
              }
            });
    syncCatalogTabsFromShell();
  }

  private void syncCatalogTabsFromShell() {
    if (catalogAuctionKindGroup == null || tabEnglishAuctions == null || tabDutchAuctions == null) {
      return;
    }
    AuctionType wanted = KhungController.getCatalogAuctionType();
    ToggleButton pick = wanted == AuctionType.DUTCH ? tabDutchAuctions : tabEnglishAuctions;
    if (!Objects.equals(catalogAuctionKindGroup.getSelectedToggle(), pick)) {
      catalogAuctionKindGroup.selectToggle(pick);
    }
  }

  private void startTimelines() {
    countdownTimeline =
        new Timeline(
            new KeyFrame(
                javafx.util.Duration.seconds(1),
                e -> {
                  trendingCardMap.values().forEach(ItemCardController::updateTimeLabel);
                  for (Map<Integer, ItemCardController> m : categoryCardMaps) {
                    m.values().forEach(ItemCardController::updateTimeLabel);
                  }
                }));
    countdownTimeline.setCycleCount(Timeline.INDEFINITE);
    countdownTimeline.play();

    autoRefreshTimeline =
        new Timeline(
            new KeyFrame(javafx.util.Duration.seconds(AUTO_REFRESH_SECONDS), e -> refreshItems()));
    autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
    autoRefreshTimeline.play();
  }

  public void stopTimelines() {
    if (countdownTimeline != null) {
      countdownTimeline.stop();
      countdownTimeline = null;
    }
    if (autoRefreshTimeline != null) {
      autoRefreshTimeline.stop();
      autoRefreshTimeline = null;
    }
  }

  public void refreshItems() {
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
                Platform.runLater(() -> cacheAndRender(list));
              } catch (Exception e) {
                LOGGER.warn("Failed to refresh auction items", e);
              }
            });
    fetchThread.setDaemon(true);
    fetchThread.start();
  }

  public void setFilters(String keyword, String category) {
    syncCatalogTabsFromShell();
    this.keyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
    this.category = (category == null || category.isBlank()) ? "All" : category;
    renderFilteredItems();
  }

  private void cacheAndRender(List<?> rawList) {
    cachedItems.clear();
    for (Object entry : rawList) {
      if (entry instanceof Item item) {
        cachedItems.add(item);
      }
    }
    renderFilteredItems();
  }

  private void renderFilteredItems() {
    if (TrendingBind == null) {
      return;
    }
    AuctionFilterContext filter = AuctionFilterContext.fromHomeState(keyword, category);
    List<Item> trendingVisible = filter.itemsMatchingTrending(cachedItems);

    rowSynchronizer.syncRow(
        TrendingBind, trendingCardMap, trendingRootByItemId, trendingVisible, false, false);

    for (int i = 0; i < TrangChuCatalogConstants.SLOT_CATEGORIES.length; i++) {
      renderCategoryRow(i, filter);
    }
  }

  private void renderCategoryRow(int index, AuctionFilterContext filter) {
    if (categoryRows == null || index < 0 || index >= categoryRows.length) {
      return;
    }
    HBox row = categoryRows[index];
    if (row == null) {
      return;
    }
    String lane = TrangChuCatalogConstants.SLOT_CATEGORIES[index];
    List<Item> allForLane = filter.itemsMatchingCategoryLane(cachedItems, lane);
    int maxSlots = TrangChuCatalogConstants.MAX_SLOTS_PER_CATEGORY;
    categoryCarouselOffset[index] =
        CategoryCarouselSupport.clampOffset(
            categoryCarouselOffset[index], allForLane.size(), maxSlots);
    List<Item> visibleWindow =
        CategoryCarouselSupport.sliceWindow(allForLane, categoryCarouselOffset[index], maxSlots);

    rowSynchronizer.syncRow(
        row,
        categoryCardMaps.get(index),
        categoryRootMaps.get(index),
        visibleWindow,
        true,
        true);

    CategoryCarouselSupport.updateNavButtons(
        allForLane.size(),
        categoryCarouselOffset[index],
        maxSlots,
        categoryPrevButtons != null ? categoryPrevButtons[index] : null,
        categoryNextButtons != null ? categoryNextButtons[index] : null);
  }

  @FXML
  void categoryCarouselPrev(ActionEvent event) {
    int idx =
        CategoryCarouselSupport.laneIndexFromAction(event, categoryPrevButtons, categoryNextButtons);
    AuctionFilterContext filter = AuctionFilterContext.fromHomeState(keyword, category);
    List<Item> all =
        filter.itemsMatchingCategoryLane(
            cachedItems, TrangChuCatalogConstants.SLOT_CATEGORIES[idx]);
    int maxSlots = TrangChuCatalogConstants.MAX_SLOTS_PER_CATEGORY;
    if (all.size() <= maxSlots) {
      return;
    }
    categoryCarouselOffset[idx] =
        CategoryCarouselSupport.clampOffset(
            categoryCarouselOffset[idx] - 1, all.size(), maxSlots);
    renderCategoryRow(idx, filter);
  }

  @FXML
  void categoryCarouselNext(ActionEvent event) {
    int idx =
        CategoryCarouselSupport.laneIndexFromAction(event, categoryPrevButtons, categoryNextButtons);
    AuctionFilterContext filter = AuctionFilterContext.fromHomeState(keyword, category);
    List<Item> all =
        filter.itemsMatchingCategoryLane(
            cachedItems, TrangChuCatalogConstants.SLOT_CATEGORIES[idx]);
    int maxSlots = TrangChuCatalogConstants.MAX_SLOTS_PER_CATEGORY;
    if (all.size() <= maxSlots) {
      return;
    }
    categoryCarouselOffset[idx] =
        CategoryCarouselSupport.clampOffset(
            categoryCarouselOffset[idx] + 1, all.size(), maxSlots);
    renderCategoryRow(idx, filter);
  }

  public void removeClosedItem(Item item) {
    if (item == null) {
      return;
    }
    cachedItems.removeIf(cached -> cached.getId() == item.getId());
    renderFilteredItems();
  }

  public void updatePriceUi(Item updated) {
    if (updated == null) {
      return;
    }
    Item cachedRef = null;
    for (Item cached : cachedItems) {
      if (cached.getId() == updated.getId()) {
        cached.setCurrentPrice(updated.getCurrentPrice());
        cached.setEndTime(updated.getEndTime());
        cachedRef = cached;
        break;
      }
    }
    if (cachedRef == null) {
      return;
    }
    ItemCardController t = trendingCardMap.get(updated.getId());
    if (t != null) {
      t.syncFromCatalogItem(cachedRef);
    }
    for (Map<Integer, ItemCardController> m : categoryCardMaps) {
      ItemCardController c = m.get(updated.getId());
      if (c != null) {
        c.syncFromCatalogItem(cachedRef);
      }
    }
  }
}
