package com.auction.client.ui.Main;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Chat.ChatPageController;
import com.auction.client.ui.History.HistoryController;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import com.auction.client.ui.Profile.ProfileController;
import com.auction.client.ui.TrangChu.TrangChuController;
import com.auction.client.ui.UserProfile.UserProfileController;
import com.auction.client.ui.YourItem.YourItemController;
import com.auction.client.util.ImagePresentationUtil;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.User;
import com.auction.shared.UserRole;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KhungController {
  private static final Logger LOGGER = LoggerFactory.getLogger(KhungController.class);
  private static KhungController instance;
  private static Pane mainContentPane;

  public static ItemInformationController itemDetailController;

  private final AuctionSearchFilterState searchFilters = new AuctionSearchFilterState();

  private MainShellNavigator navigator;
  private MainShellNetworkBridge networkBridge;

  // ĐÃ THÊM watchlistNode
  private Node auctionHomeNode, watchlistNode, liveAuctionNode, vipMembershipNode, historyNode, myItemsNode, profileNode, adminDashboardNode, addLotNode, chatNode;
  private TrangChuController homeController;
  private YourItemController myItemsController;
  private HistoryController historyController;
  private ProfileController profileController;
  private AdminDashboardController adminDashboardController;
  private ChatPageController chatCtrl;
  private com.auction.client.ui.AddNewLot.AddNewLotController addLotController;

  // ĐÃ THÊM watchlistController
  private com.auction.client.ui.Watchlist.WatchlistController watchlistController;
  private com.auction.client.ui.Live.LiveAuctionController liveAuctionController;
  private com.auction.client.ui.Vip.VipMembershipController vipMembershipController;

  @FXML private HBox SearchContainer;
  @FXML private StackPane ContentArea;

  @FXML private HBox AuctionMenu, WatchlistMenu, LiveMenu, VipMenu, HistoryMenu, MyItemMenu, ProfileMenu, ChatMenu, ManageUsersMenu;
  @FXML private Label UserName, Rank;
  @FXML private ImageView sidebaravatar;

  private <T extends Node> NodeContentLoader<T> loadFxml(String path) throws Exception {
    NodeContentLoader<T> loader = new NodeContentLoader<>();
    loader.load(path);
    return loader;
  }

  @FXML
  public void initialize() {
    instance = this;
    mainContentPane = ContentArea;
    try {
      NodeContentLoader<HBox> search = loadFxml("/fxml/searchbar/ThanhTimKiem.fxml");
      NodeContentLoader<Pane> auction = loadFxml("/fxml/trangchu/TrangChu.fxml");

      // ĐÃ THÊM LOAD WATCHLIST FXML
      NodeContentLoader<Pane> watchlist = loadFxml("/fxml/watchlist/Watchlist.fxml");
      NodeContentLoader<Pane> liveAuction = loadFxml("/fxml/live/LiveAuction.fxml");
      NodeContentLoader<Pane> vipMembership = loadFxml("/fxml/vip/VipMembership.fxml");

      NodeContentLoader<Pane> hist = loadFxml("/fxml/history/History.fxml");
      NodeContentLoader<Pane> myItem = loadFxml("/fxml/youritem/YourItem.fxml");
      NodeContentLoader<Pane> profile = loadFxml("/fxml/profile/Profile.fxml");
      NodeContentLoader<Pane> admin = loadFxml("/fxml/main/AdminDashboard.fxml");
      NodeContentLoader<Pane> addLot = loadFxml("/fxml/addnewlot/AddNewLot.fxml");
      NodeContentLoader<Pane> chat = loadFxml("/fxml/chat/ChatPage.fxml");

      if (ContentArea != null) ContentArea.getChildren().add(auction.getCurrentNode());
      if (SearchContainer != null) SearchContainer.getChildren().add(search.getCurrentNode());

      auctionHomeNode = auction.getCurrentNode();
      watchlistNode = watchlist.getCurrentNode();
      liveAuctionNode = liveAuction.getCurrentNode();
      vipMembershipNode = vipMembership.getCurrentNode();
      historyNode = hist.getCurrentNode();
      myItemsNode = myItem.getCurrentNode();
      profileNode = profile.getCurrentNode();
      adminDashboardNode = admin.getCurrentNode();
      addLotNode = addLot.getCurrentNode();
      chatNode = chat.getCurrentNode();

      homeController = auction.getController();
      watchlistController = watchlist.getController();
      liveAuctionController = liveAuction.getController();
      vipMembershipController = vipMembership.getController();
      myItemsController = myItem.getController();
      historyController = hist.getController();
      profileController = profile.getController();
      adminDashboardController = admin.getController();
      addLotController = addLot.getController();
      chatCtrl = chat.getController();

      // ĐÃ THÊM WatchlistMenu VÀO NAVIGATOR
      navigator =
              new MainShellNavigator(
                      ContentArea,
                      AuctionMenu,
                      WatchlistMenu,
                      LiveMenu,
                      VipMenu,
                      HistoryMenu,
                      MyItemMenu,
                      ProfileMenu,
                      ChatMenu,
                      ManageUsersMenu,
                      auctionHomeNode);
      navigator.setMenu(AuctionMenu);

      networkBridge = new MainShellNetworkBridge(() -> homeController, () -> chatCtrl);
      NetworkClient.getInstance().addListener(networkBridge);
      update();

      Platform.runLater(
              () -> {
                Scene scene = mainContentPane.getScene();
                if (scene != null)
                  scene.addEventFilter(
                          javafx.scene.input.KeyEvent.KEY_PRESSED,
                          event -> {
                            if (event.getCode() == javafx.scene.input.KeyCode.F11) {
                              Stage stage = (Stage) scene.getWindow();
                              stage.setFullScreen(!stage.isFullScreen());
                              event.consume();
                            }
                          });
              });
    } catch (Exception e) {
      LOGGER.warn("Main shell initialize failed", e);
    }
  }

  private void refreshAuctionHome() {
    if (homeController == null) return;
    homeController.setFilters(searchFilters.getKeyword(), searchFilters.getCategory());
    homeController.refreshItems();
  }

  private boolean switchPage(Node targetContent, HBox activeMenu) {
    return navigator != null && navigator.switchPage(targetContent, activeMenu);
  }

  @FXML
  public void openAuction(MouseEvent event) {
    if (switchPage(auctionHomeNode, AuctionMenu)) refreshAuctionHome();
  }

  // ĐÃ THÊM HÀM MỞ WATCHLIST
  @FXML
  public void openWatchlist(MouseEvent event) {
    if (switchPage(watchlistNode, WatchlistMenu) && watchlistController != null) {
      watchlistController.refreshItems();
    }
  }

  @FXML
  public void openLiveAuction(MouseEvent event) {
    if (switchPage(liveAuctionNode, LiveMenu) && liveAuctionController != null) {
      liveAuctionController.refreshOnNavigate();
    }
  }

  @FXML
  public void openVipMembership(MouseEvent event) {
    if (switchPage(vipMembershipNode, VipMenu) && vipMembershipController != null) {
      vipMembershipController.refreshOnNavigate();
    }
  }

  @FXML
  public void openHistory(MouseEvent event) {
    if (switchPage(historyNode, HistoryMenu) && historyController != null) historyController.refreshHistory();
  }

  @FXML
  public void openMyItems(MouseEvent event) {
    if (switchPage(myItemsNode, MyItemMenu) && myItemsController != null) myItemsController.refreshItems();
  }

  @FXML
  public void openProfile(MouseEvent event) {
    if (switchPage(profileNode, ProfileMenu) && profileController != null) profileController.refreshFromServer();
  }

  @FXML
  public void openChat(MouseEvent event) {
    if (switchPage(chatNode, ChatMenu) && chatCtrl != null) chatCtrl.refreshOnNavigate();
  }

  @FXML
  public void openManageUsers(MouseEvent event) {
    if (switchPage(adminDashboardNode, ManageUsersMenu) && adminDashboardController != null)
      adminDashboardController.refreshDashboard();
  }

  @FXML
  public void handleRefresh(ActionEvent event) {
    Node current = navigator != null ? navigator.getCurrentContentNode() : null;
    if (current == auctionHomeNode) refreshAuctionHome();
    else if (current == watchlistNode && watchlistController != null) watchlistController.refreshItems();
    else if (current == liveAuctionNode && liveAuctionController != null) liveAuctionController.refreshOnNavigate();
    else if (current == vipMembershipNode && vipMembershipController != null) vipMembershipController.refreshOnNavigate();
    else if (current == historyNode && historyController != null) historyController.refreshHistory();
    else if (current == myItemsNode && myItemsController != null) myItemsController.refreshItems();
    else if (current == profileNode && profileController != null) profileController.refreshFromServer();
    else if (current == chatNode && chatCtrl != null) chatCtrl.refreshOnNavigate();
    else if (current == adminDashboardNode && adminDashboardController != null)
      adminDashboardController.refreshDashboard();
    else if (itemDetailController != null) itemDetailController.refresh();
  }

  @FXML
  public void handlePrimaryAction(ActionEvent event) {
    if (ClientSession.getActiveRole() == UserRole.SELLER) {
      Node current = navigator != null ? navigator.getCurrentContentNode() : null;
      if (current != addLotNode) com.auction.client.ui.AddNewLot.AddNewLotController.resetWhenOpening();
      switchPage(addLotNode, AuctionMenu);
    } else if (switchPage(auctionHomeNode, AuctionMenu)) {
      refreshAuctionHome();
    }
  }

  @FXML
  public void handleSignout() {
    performUserSignOut();
  }

  public void update() {
    User currentUser = ClientSession.getCurrentUser();
    if (currentUser == null) return;
    if (UserName != null) UserName.setText(ClientSession.getUsername());
    if (Rank != null) {
      String rankText = ClientSession.getActiveRole().name();
      if (currentUser.isVip()) {
        rankText = "VIP | " + rankText;
      }
      if (currentUser.getTotalRatings() > 0)
        rankText +=
                " | "
                        + String.format(
                        "%.1f\u2605 %s",
                        currentUser.getAvgRating(),
                        currentUser.getAvgRating() <= 2.0
                                ? "Negative"
                                : currentUser.getAvgRating() <= 3.0 ? "Neutral" : "Positive");
      Rank.setText(rankText);
    }
    boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
    if (ManageUsersMenu != null) {
      ManageUsersMenu.setVisible(isAdmin);
      ManageUsersMenu.setManaged(isAdmin);
    }
    if (sidebaravatar != null
            && currentUser.getAvatarUrl() != null
            && !currentUser.getAvatarUrl().isBlank())
      ImagePresentationUtil.loadCircularAvatar(sidebaravatar, currentUser.getAvatarUrl(), 24, 48);
  }

  static void notifyHomePriceUpdate(Item item) {
    if (instance != null && instance.homeController != null) instance.homeController.updatePriceUi(item);
  }

  // ĐÃ THÊM CẬP NHẬT WATCHLIST CONTROLLER
  public static void notifyWatchlistToggle(int itemId, boolean isWatched) {
    if (instance == null) return;
    Platform.runLater(() -> {
      if (instance.homeController != null) instance.homeController.updateWatchlistUi(itemId, isWatched);
      if (instance.historyController != null) instance.historyController.updateWatchlistUi(itemId, isWatched);
      if (instance.myItemsController != null) instance.myItemsController.updateWatchlistUi(itemId, isWatched);
      if (instance.watchlistController != null) instance.watchlistController.updateWatchlistUi(itemId, isWatched);
    });
  }

  /** Called from {@link MainShellNetworkBridge} after account-ban alert. */
  public static void performForcedLogoutFromServer() {
    if (instance != null) instance.performUserSignOut();
  }

  private void performUserSignOut() {
    ClientSession.clear();
    if (networkBridge != null) NetworkClient.getInstance().removeListener(networkBridge);
    navigateToLogin();
  }

  private void navigateToLogin() {
    try {
      Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
      Stage stage = (Stage) ContentArea.getScene().getWindow();
      stage.setScene(new Scene(root));
      stage.centerOnScreen();
      stage.show();
    } catch (Exception ex) {
      LOGGER.error("Forced logout navigation failed", ex);
    }
  }

  public static Pane getMainContentPane() {
    return mainContentPane;
  }

  public static Node getCurrentNode() {
    return instance != null && instance.navigator != null ? instance.navigator.getCurrentContentNode() : null;
  }

  public static void setMainContentNode(Node n) {
    if (n != null && instance != null && instance.navigator != null) instance.navigator.setCurrentContentNode(n);
  }

  public static void refreshSidebarFromSession() {
    if (instance != null) instance.update();
  }

  public static String getSearchKeyword() {
    return instance != null ? instance.searchFilters.getKeyword() : "";
  }

  public static String getCategoryFilter() {
    return instance != null ? instance.searchFilters.getCategory() : "All";
  }

  public static double getMinPrice() {
    return instance != null ? instance.searchFilters.getMinPrice() : 0;
  }

  public static double getMaxPrice() {
    return instance != null ? instance.searchFilters.getMaxPrice() : Double.MAX_VALUE;
  }

  public static AuctionType getCatalogAuctionType() {
    return instance != null
            ? instance.searchFilters.getCatalogAuctionType()
            : AuctionType.ENGLISH;
  }

  public static void setCatalogAuctionType(AuctionType auctionType) {
    if (instance == null) return;
    instance.searchFilters.setCatalogAuctionType(
            auctionType != null ? auctionType : AuctionType.ENGLISH);
    if (instance.homeController != null) {
      instance.homeController.setFilters(
              instance.searchFilters.getKeyword(), instance.searchFilters.getCategory());
    }
    Node current = getCurrentNode();
    if (instance.historyController != null && current != null && current == instance.historyNode) {
      instance.historyController.refreshHistory();
    }
  }

  public static void applySearchFilter(String keyword, String category, double min, double max) {
    if (instance == null) return;
    instance.searchFilters.apply(keyword, category, min, max);
    if (instance.homeController != null)
      instance.homeController.setFilters(instance.searchFilters.getKeyword(), instance.searchFilters.getCategory());
    if (instance.myItemsController != null) instance.myItemsController.refreshItems();
  }

  /**
   * After editing a pending lot from My Items — returns to seller list (not home).
   */
  public static void returnFromLotEditor(boolean savedSuccessfully) {
    if (instance == null || instance.navigator == null) return;
    instance.navigator.switchPage(instance.myItemsNode, instance.MyItemMenu);
    if (savedSuccessfully && instance.myItemsController != null) {
      instance.myItemsController.refreshItems();
    }
  }

  public static void openEditPendingItem(Item item) {
    if (instance == null || item == null || instance.addLotController == null || instance.navigator == null)
      return;
    instance.addLotController.openForEdit(item);
    instance.navigator.switchPage(instance.addLotNode, instance.MyItemMenu);
  }

  public static void returnFromAddLot(boolean refreshAfterSubmit) {
    if (instance == null || instance.navigator == null) return;
    instance.navigator.switchPage(instance.auctionHomeNode, instance.AuctionMenu);
    if (refreshAfterSubmit && instance.homeController != null) {
      instance.homeController.setFilters(getSearchKeyword(), getCategoryFilter());
      instance.homeController.refreshItems();
    }
  }

  public static void showUserProfile(User user) {
    if (instance == null || user == null || instance.navigator == null) return;
    Platform.runLater(
            () -> {
              try {
                NodeContentLoader<Parent> profileLoader =
                        instance.loadFxml("/fxml/userprofile/UserProfile.fxml");
                profileLoader.<UserProfileController>getController().setUser(user);
                instance.navigator.replaceContent(profileLoader.getCurrentNode(), null);
              } catch (Exception ignored) {
              }
            });
  }

  public static void returnToAuction() {
    if (instance == null || instance.navigator == null) return;
    Platform.runLater(
            () -> {
              if (instance.navigator.switchPage(instance.auctionHomeNode, instance.AuctionMenu))
                instance.refreshAuctionHome();
            });
  }

  /** Opens Live Auction screen and joins the given lot if available. */
  public static void openLiveAuctionForItem(int itemId) {
    if (instance == null || instance.navigator == null || itemId <= 0) {
      return;
    }
    Platform.runLater(
        () -> {
          instance.navigator.switchPage(instance.liveAuctionNode, instance.LiveMenu);
          if (instance.liveAuctionController != null) {
            instance.liveAuctionController.openForItem(itemId);
          }
        });
  }

  /** Holds auction home / my-items filter criteria for the main shell (no UI). */
  private static final class AuctionSearchFilterState {

    private String keyword = "";
    private String category = "All";
    private double minPrice = 0;
    private double maxPrice = Double.MAX_VALUE;
    private AuctionType catalogAuctionType = AuctionType.ENGLISH;

    String getKeyword() {
      return keyword;
    }

    String getCategory() {
      return category;
    }

    double getMinPrice() {
      return minPrice;
    }

    double getMaxPrice() {
      return maxPrice;
    }

    AuctionType getCatalogAuctionType() {
      return catalogAuctionType != null ? catalogAuctionType : AuctionType.ENGLISH;
    }

    void setCatalogAuctionType(AuctionType auctionType) {
      this.catalogAuctionType = auctionType != null ? auctionType : AuctionType.ENGLISH;
    }

    void apply(String keyword, String category, double min, double max) {
      this.keyword = keyword == null ? "" : keyword.trim();
      this.category = category == null ? "All" : category.trim();
      this.minPrice = min;
      this.maxPrice = max;
    }
  }
}