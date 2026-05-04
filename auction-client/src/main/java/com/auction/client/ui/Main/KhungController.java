package com.auction.client.ui.Main;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.ui.Chat.ChatPageController;
import com.auction.client.ui.History.HistoryController;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import com.auction.client.ui.TrangChu.TrangChuController;
import com.auction.client.ui.Profile.ProfileController;
import com.auction.client.ui.UserProfile.UserProfileController;
import com.auction.client.ui.YourItem.YourItemController;
import com.auction.client.util.NotificationCenter;
import com.auction.shared.ChatMessage;
import com.auction.shared.Friendship;
import com.auction.shared.Item;
import com.auction.shared.User;
import com.auction.shared.UserRole;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import com.auction.client.network.NetworkClient;
import com.auction.client.network.NetworkEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KhungController implements NetworkEventListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(KhungController.class);
  private static KhungController instance;
  private static Pane mainContentPane;
  private static Node currentContentNode;
  private static String searchKeyword = "";
  private static String categoryFilter = "All";
  private static double filterMinPrice = 0;
  private static double filterMaxPrice = Double.MAX_VALUE;
  public static ItemInformationController itemDetailController;

  private Node auctionHomeNode, historyNode, myItemsNode, profileNode, adminDashboardNode, addLotNode, chatNode;
  private TrangChuController homeController;
  private YourItemController myItemsController;
  private HistoryController historyController;
  private ProfileController profileController;
  private AdminDashboardController adminDashboardController;
  private ChatPageController chatCtrl;

  @FXML private HBox SearchContainer;
  @FXML private StackPane ContentArea;
  @FXML private HBox AuctionMenu, HistoryMenu, MyItemMenu, ProfileMenu, ChatMenu, ManageUsersMenu;
  @FXML private Label UserName, Rank;
  @FXML private Button primaryactionbutton;
  @FXML private ImageView sidebaravatar;

  private <T extends javafx.scene.Node> NodeContentLoader<T> loadFxml(String path) throws Exception {
    NodeContentLoader<T> loader = new NodeContentLoader<>(); loader.load(path); return loader;
  }

  @FXML
  public void initialize() {
    instance = this; mainContentPane = ContentArea;
    NetworkClient.getInstance().addListener(this);
    try {
      NodeContentLoader<HBox> search = loadFxml("/fxml/searchbar/ThanhTimKiem.fxml");
      NodeContentLoader<Pane> auction = loadFxml("/fxml/trangchu/TrangChu.fxml");
      NodeContentLoader<Pane> hist = loadFxml("/fxml/history/History.fxml");
      NodeContentLoader<Pane> myItem = loadFxml("/fxml/youritem/YourItem.fxml");
      NodeContentLoader<Pane> profile = loadFxml("/fxml/profile/Profile.fxml");
      NodeContentLoader<Pane> admin = loadFxml("/fxml/main/AdminDashboard.fxml");
      NodeContentLoader<Pane> addLot = loadFxml("/fxml/addnewlot/AddNewLot.fxml");
      NodeContentLoader<Pane> chat = loadFxml("/fxml/chat/ChatPage.fxml");
      if (ContentArea != null) ContentArea.getChildren().add(auction.getCurrentNode());
      if (SearchContainer != null) SearchContainer.getChildren().add(search.getCurrentNode());
      auctionHomeNode = auction.getCurrentNode(); historyNode = hist.getCurrentNode(); myItemsNode = myItem.getCurrentNode();
      profileNode = profile.getCurrentNode(); adminDashboardNode = admin.getCurrentNode(); addLotNode = addLot.getCurrentNode(); chatNode = chat.getCurrentNode();
      homeController = auction.getController();
      myItemsController = myItem.getController();
      historyController = hist.getController();
      profileController = profile.getController();
      adminDashboardController = admin.getController();
      chatCtrl = chat.getController();
      currentContentNode = auctionHomeNode; setMenu(AuctionMenu); update();
      Platform.runLater(() -> {
        javafx.scene.Scene scene = mainContentPane.getScene();
        if (scene != null) scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
          if (event.getCode() == javafx.scene.input.KeyCode.F11) {
            javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
            stage.setFullScreen(!stage.isFullScreen()); event.consume();
          }
        });
      });
    } catch (Exception e) {}
  }

  private void refreshAuctionHome() {
    if (homeController == null) return;
    homeController.setFilters(getSearchKeyword(), getCategoryFilter());
    homeController.refreshItems();
  }

  @FXML public void openAuction(MouseEvent event) {
    if (switchPage(auctionHomeNode, AuctionMenu)) refreshAuctionHome();
  }
  @FXML public void openHistory(MouseEvent event) {
    if (switchPage(historyNode, HistoryMenu) && historyController != null) historyController.refreshHistory();
  }
  @FXML public void openMyItems(MouseEvent event) {
    if (switchPage(myItemsNode, MyItemMenu) && myItemsController != null) myItemsController.refreshItems();
  }
  @FXML public void openProfile(MouseEvent event) {
    if (switchPage(profileNode, ProfileMenu) && profileController != null) profileController.refreshFromServer();
  }
  @FXML public void openChat(MouseEvent event) {
    if (switchPage(chatNode, ChatMenu) && chatCtrl != null) chatCtrl.refreshOnNavigate();
  }
  @FXML public void openManageUsers(MouseEvent event) {
    if (switchPage(adminDashboardNode, ManageUsersMenu) && adminDashboardController != null)
      adminDashboardController.refreshDashboard();
  }

  @FXML public void handleRefresh(ActionEvent event) {
    if (currentContentNode == auctionHomeNode) refreshAuctionHome();
    else if (currentContentNode == historyNode && historyController != null) historyController.refreshHistory();
    else if (currentContentNode == myItemsNode && myItemsController != null) myItemsController.refreshItems();
    else if (currentContentNode == profileNode && profileController != null) profileController.refreshFromServer();
    else if (currentContentNode == chatNode && chatCtrl != null) chatCtrl.refreshOnNavigate();
    else if (currentContentNode == adminDashboardNode && adminDashboardController != null)
      adminDashboardController.refreshDashboard();
    else if (itemDetailController != null) itemDetailController.refresh();
  }
  @FXML public void handlePrimaryAction(ActionEvent event) {
    if (ClientSession.getActiveRole() == UserRole.SELLER) {
      if (currentContentNode != addLotNode) com.auction.client.ui.AddNewLot.AddNewLotController.resetWhenOpening();
      switchPage(addLotNode, AuctionMenu);
    } else if (switchPage(auctionHomeNode, AuctionMenu)) {
      refreshAuctionHome();
    }
  }
  @FXML public void handleSignout() { performForcedLogout(); }

  /** @return true if the visible root actually changed (so callers can avoid redundant network refresh). */
  private boolean switchPage(Node targetContent, HBox activeMenu) {
    if (targetContent == null || currentContentNode == targetContent) return false;
    if (ContentArea != null) { ContentArea.getChildren().clear(); ContentArea.getChildren().add(targetContent); }
    currentContentNode = targetContent; setMenu(activeMenu);
    return true;
  }

  private void setMenu(HBox active) {
    for (HBox m : new HBox[]{AuctionMenu, HistoryMenu, MyItemMenu, ProfileMenu, ChatMenu, ManageUsersMenu})
      if (m != null) m.getStyleClass().remove("active");
    if (active != null && !active.getStyleClass().contains("active")) active.getStyleClass().add("active");
  }

  public void update() {
    User currentUser = ClientSession.getCurrentUser();
    if (currentUser == null) return;
    if (UserName != null) UserName.setText(ClientSession.getUsername());
    if (Rank != null) {
      String rankText = ClientSession.getActiveRole().name();
      if (currentUser.getTotalRatings() > 0) rankText += " | " + String.format("%.1f\u2605 %s", currentUser.getAvgRating(), currentUser.getAvgRating() <= 2.0 ? "Negative" : currentUser.getAvgRating() <= 3.0 ? "Neutral" : "Positive");
      Rank.setText(rankText);
    }
    boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
    if (ManageUsersMenu != null) { ManageUsersMenu.setVisible(isAdmin); ManageUsersMenu.setManaged(isAdmin); }
    if (sidebaravatar != null && currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isBlank()) loadSidebarAvatar(currentUser.getAvatarUrl());
  }

  private void loadSidebarAvatar(String url) {
    Image img = new Image(url, true);
    img.progressProperty().addListener((obs, ov, nv) -> {
      if (nv.doubleValue() == 1.0 && !img.isError()) Platform.runLater(() -> {
        double w = img.getWidth(), h = img.getHeight(), side = Math.min(w, h);
        sidebaravatar.setImage(img);
        sidebaravatar.setViewport(new javafx.geometry.Rectangle2D((w - side) / 2, (h - side) / 2, side, side));
        sidebaravatar.setFitWidth(48); sidebaravatar.setFitHeight(48); sidebaravatar.setPreserveRatio(false);
        sidebaravatar.setClip(new Circle(24, 24, 24));
      });
    });
  }

  public static Pane getMainContentPane() { return mainContentPane; }
  public static Node getCurrentNode() { return currentContentNode; }
  public static void setMainContentNode(Node n) { if (n != null) currentContentNode = n; }
  public static void refreshSidebarFromSession() { if (instance != null) instance.update(); }
  public static String getSearchKeyword() { return searchKeyword; }
  public static String getCategoryFilter() { return categoryFilter; }
  public static double getMinPrice() { return filterMinPrice; }
  public static double getMaxPrice() { return filterMaxPrice; }

  public static void applySearchFilter(String keyword, String category, double min, double max) {
    searchKeyword = keyword == null ? "" : keyword.trim(); categoryFilter = category == null ? "All" : category.trim();
    filterMinPrice = min; filterMaxPrice = max;
    if (instance != null) {
      if (instance.homeController != null) instance.homeController.setFilters(searchKeyword, categoryFilter);
      if (instance.myItemsController != null) instance.myItemsController.refreshItems();
    }
  }

  public static void returnFromAddLot(boolean refreshAfterSubmit) {
    if (instance == null) return;
    instance.switchPage(instance.auctionHomeNode, instance.AuctionMenu);
    if (refreshAfterSubmit && instance.homeController != null) {
      instance.homeController.setFilters(getSearchKeyword(), getCategoryFilter());
      instance.homeController.refreshItems();
    }
  }

  public static void showUserProfile(User user) {
    if (instance == null || user == null) return;
    Platform.runLater(() -> { try {
      NodeContentLoader<javafx.scene.Parent> profileLoader = instance.loadFxml("/fxml/userprofile/UserProfile.fxml");
      profileLoader.<UserProfileController>getController().setUser(user); currentContentNode = profileLoader.getCurrentNode();
      if (instance.ContentArea != null) { instance.ContentArea.getChildren().clear(); instance.ContentArea.getChildren().add(currentContentNode); }
      instance.setMenu(null);
    } catch (Exception ignored) {} });
  }

  public static void returnToAuction() {
    if (instance == null) return;
    Platform.runLater(() -> {
      if (instance.switchPage(instance.auctionHomeNode, instance.AuctionMenu)) instance.refreshAuctionHome();
    });
  }

  public static void updateRealtimeUi(Item item) {
    Platform.runLater(() -> {
      if (itemDetailController != null) itemDetailController.updatePriceUi(item);
      if (instance != null && instance.homeController != null) instance.homeController.updatePriceUi(item);
    });
  }

  @Override public void onNewBidUpdate(Item item) { if (itemDetailController != null) itemDetailController.updatePriceUi(item); if (homeController != null) homeController.updatePriceUi(item); }

  @Override
  public void onItemClosed(Item item) {
    if (homeController != null) homeController.removeClosedItem(item);
    if (itemDetailController != null) itemDetailController.markItemClosed(item);
    User me = ClientSession.getCurrentUser();
    if (me != null && item.getSellerId() == me.getId()) NotificationCenter.addNotification("Sản phẩm \"" + item.getName() + "\" đã được đóng đấu giá!");
  }

  @Override public void onSellerBidNotify(Item item, double newBidPrice) { NotificationCenter.addNotification("Sản phẩm \"" + item.getName() + "\" có bid mới: " + String.format("%,.0f", newBidPrice) + " VND"); }

  @Override public void onOutbidNotify(int itemId) { NotificationCenter.addNotification("Bạn đã bị vượt giá ở item #" + itemId + "!"); }
  @Override public void onGlobalChat(ChatMessage message) { if (chatCtrl != null) chatCtrl.onGlobalChat(message); }
  @Override public void onPrivateChat(ChatMessage message) { if (chatCtrl != null) chatCtrl.onPrivateChat(message); }

  @Override public void onFriendRequest(Friendship friendship) {
    NotificationCenter.addNotification(friendship.getRequesterUsername() + " đã gửi lời mời kết bạn!");
    if (chatCtrl != null) chatCtrl.onFriendRequest(friendship);
  }
  @Override public void onFriendRequestSent(Friendship friendship) {
    NotificationCenter.addNotification("Đã gửi lời mời kết bạn đến " + friendship.getAddresseeUsername() + "!");
    if (chatCtrl != null) chatCtrl.onFriendRequestSent(friendship);
  }
  @Override public void onFriendAccepted(Friendship friendship) {
    if (chatCtrl != null) chatCtrl.onFriendAccepted(friendship);
    User me = ClientSession.getCurrentUser();
    NotificationCenter.addNotification(me != null && friendship.getRequesterId() == me.getId()
        ? friendship.getAddresseeUsername() + " đã chấp nhận lời mời kết bạn!"
        : "Bạn và " + friendship.getRequesterUsername() + " đã trở thành bạn bè!");
  }

  @Override public void onAccountBanned(String reason) {
    showFxAlert(javafx.scene.control.Alert.AlertType.ERROR, "Account Suspended",
        "Your account has been suspended by an administrator.",
        reason != null && !reason.isBlank() ? reason : "You have been banned. Please contact support.");
    performForcedLogout();
  }
  @Override public void onAccountUnbanned() {
    showFxAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Account Reinstated",
        "Your account is active again.", "An administrator has lifted your suspension. Please log in to continue.");
  }

  private void showFxAlert(javafx.scene.control.Alert.AlertType type, String title, String header, String content) {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
    alert.setTitle(title); alert.setHeaderText(header); alert.setContentText(content); alert.showAndWait();
  }

  private void performForcedLogout() {
    ClientSession.clear(); NetworkClient.getInstance().removeListener(this);
    try {
      javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
      javafx.stage.Stage stage = (javafx.stage.Stage) ContentArea.getScene().getWindow();
      stage.setScene(new javafx.scene.Scene(root)); stage.centerOnScreen(); stage.show();
    } catch (Exception ex) { LOGGER.error("Forced logout navigation failed", ex); }
  }
}
