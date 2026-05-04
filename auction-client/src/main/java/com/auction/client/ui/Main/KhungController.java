package com.auction.client.ui.Main;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.ui.Chat.ChatPageController;
import com.auction.client.ui.History.HistoryController;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import com.auction.client.ui.TrangChu.TrangChuController;
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

  private Node an, hn, mn, pn, adn, aln, chn;
  private TrangChuController tc;
  private YourItemController yc;
  private HistoryController hc;
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
      an = auction.getCurrentNode(); hn = hist.getCurrentNode(); mn = myItem.getCurrentNode();
      pn = profile.getCurrentNode(); adn = admin.getCurrentNode(); aln = addLot.getCurrentNode(); chn = chat.getCurrentNode();
      tc = auction.getController(); yc = myItem.getController(); hc = hist.getController(); chatCtrl = chat.getController();
      currentContentNode = an; setMenu(AuctionMenu); update();
      Platform.runLater(() -> {
        javafx.scene.Scene scene = mainContentPane.getScene();
        if (scene != null) scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
          if (e.getCode() == javafx.scene.input.KeyCode.F11) {
            javafx.stage.Stage s = (javafx.stage.Stage) scene.getWindow();
            s.setFullScreen(!s.isFullScreen()); e.consume();
          }
        });
      });
    } catch (Exception e) {}
  }

  @FXML public void openAuction(MouseEvent e) { switchPage(an, AuctionMenu); }
  @FXML public void openHistory(MouseEvent e) { switchPage(hn, HistoryMenu); if (hc != null) hc.refreshHistory(); }
  @FXML public void openMyItems(MouseEvent e) { switchPage(mn, MyItemMenu); }
  @FXML public void openProfile(MouseEvent e) { switchPage(pn, ProfileMenu); }
  @FXML public void openChat(MouseEvent e) { switchPage(chn, ChatMenu); }
  @FXML public void openManageUsers(MouseEvent e) { switchPage(adn, ManageUsersMenu); }

  @FXML public void handleRefresh(ActionEvent e) {
    if (currentContentNode == an && tc != null) tc.refreshItems();
    else if (currentContentNode == hn && hc != null) hc.refreshHistory();
    else if (currentContentNode == mn && yc != null) yc.refreshItems();
    else if (itemDetailController != null) itemDetailController.refresh();
  }
  @FXML public void handlePrimaryAction(ActionEvent e) {
    if (ClientSession.getActiveRole() == UserRole.SELLER) { if (currentContentNode != aln) com.auction.client.ui.AddNewLot.AddNewLotController.resetWhenOpening(); switchPage(aln, AuctionMenu); }
    else switchPage(an, AuctionMenu);
  }
  @FXML public void handleSignout() { performForcedLogout(); }
  private void switchPage(Node t, HBox m) {
    if (t == null || currentContentNode == t) return;
    if (ContentArea != null) { ContentArea.getChildren().clear(); ContentArea.getChildren().add(t); }
    currentContentNode = t; setMenu(m);
  }

  private void setMenu(HBox active) {
    for (HBox m : new HBox[]{AuctionMenu, HistoryMenu, MyItemMenu, ProfileMenu, ChatMenu, ManageUsersMenu})
      if (m != null) m.getStyleClass().remove("active");
    if (active != null && !active.getStyleClass().contains("active")) active.getStyleClass().add("active");
  }

  public void update() {
    User u = ClientSession.getCurrentUser();
    if (u == null) return;
    if (UserName != null) UserName.setText(ClientSession.getUsername());
    if (Rank != null) {
      String r = ClientSession.getActiveRole().name();
      if (u.getTotalRatings() > 0) r += " | " + String.format("%.1f\u2605 %s", u.getAvgRating(), u.getAvgRating() <= 2.0 ? "Negative" : u.getAvgRating() <= 3.0 ? "Neutral" : "Positive");
      Rank.setText(r);
    }
    boolean admin = u.getRole() == UserRole.ADMIN;
    if (ManageUsersMenu != null) { ManageUsersMenu.setVisible(admin); ManageUsersMenu.setManaged(admin); }
    if (sidebaravatar != null && u.getAvatarUrl() != null && !u.getAvatarUrl().isBlank()) loadSidebarAvatar(u.getAvatarUrl());
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

  public static void applySearchFilter(String k, String c, double min, double max) {
    searchKeyword = k == null ? "" : k.trim(); categoryFilter = c == null ? "All" : c.trim();
    filterMinPrice = min; filterMaxPrice = max;
    if (instance != null) {
      if (instance.tc != null) instance.tc.setFilters(searchKeyword, categoryFilter);
      if (instance.yc != null) instance.yc.refreshItems();
    }
  }

  public static void returnFromAddLot(boolean r) {
    if (instance == null) return;
    instance.switchPage(instance.an, instance.AuctionMenu);
    if (r && instance.tc != null) instance.tc.refreshItems();
  }

  public static void showUserProfile(User user) {
    if (instance == null || user == null) return;
    Platform.runLater(() -> { try {
      NodeContentLoader<javafx.scene.Parent> l = instance.loadFxml("/fxml/userprofile/UserProfile.fxml");
      l.<UserProfileController>getController().setUser(user); currentContentNode = l.getCurrentNode();
      if (instance.ContentArea != null) { instance.ContentArea.getChildren().clear(); instance.ContentArea.getChildren().add(currentContentNode); }
      instance.setMenu(null);
    } catch (Exception e) {} });
  }

  public static void returnToAuction() { if (instance != null) Platform.runLater(() -> instance.switchPage(instance.an, instance.AuctionMenu)); }

  public static void updateRealtimeUi(Item item) {
    Platform.runLater(() -> {
      if (itemDetailController != null) itemDetailController.updatePriceUi(item);
      if (instance != null && instance.tc != null) instance.tc.updatePriceUi(item);
    });
  }

  @Override public void onNewBidUpdate(Item item) { if (itemDetailController != null) itemDetailController.updatePriceUi(item); if (tc != null) tc.updatePriceUi(item); }

  @Override
  public void onItemClosed(Item item) {
    if (tc != null) tc.removeClosedItem(item);
    if (itemDetailController != null) itemDetailController.markItemClosed(item);
    User me = ClientSession.getCurrentUser();
    if (me != null && item.getSellerId() == me.getId()) NotificationCenter.addNotification("Sản phẩm \"" + item.getName() + "\" đã được đóng đấu giá!");
  }

  @Override public void onSellerBidNotify(Item item, double p) { NotificationCenter.addNotification("Sản phẩm \"" + item.getName() + "\" có bid mới: " + String.format("%,.0f", p) + " VND"); }

  @Override public void onOutbidNotify(int itemId) { NotificationCenter.addNotification("Bạn đã bị vượt giá ở item #" + itemId + "!"); }
  @Override public void onGlobalChat(ChatMessage m) { if (chatCtrl != null) chatCtrl.onGlobalChat(m); }
  @Override public void onPrivateChat(ChatMessage m) { if (chatCtrl != null) chatCtrl.onPrivateChat(m); }

  @Override public void onFriendRequest(Friendship f) {
    NotificationCenter.addNotification(f.getRequesterUsername() + " đã gửi lời mời kết bạn!");
    if (chatCtrl != null) chatCtrl.onFriendRequest(f);
  }
  @Override public void onFriendRequestSent(Friendship f) {
    NotificationCenter.addNotification("Đã gửi lời mời kết bạn đến " + f.getAddresseeUsername() + "!");
    if (chatCtrl != null) chatCtrl.onFriendRequestSent(f);
  }
  @Override public void onFriendAccepted(Friendship f) {
    if (chatCtrl != null) chatCtrl.onFriendAccepted(f);
    User me = ClientSession.getCurrentUser();
    NotificationCenter.addNotification(me != null && f.getRequesterId() == me.getId()
        ? f.getAddresseeUsername() + " đã chấp nhận lời mời kết bạn!"
        : "Bạn và " + f.getRequesterUsername() + " đã trở thành bạn bè!");
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
    javafx.scene.control.Alert a = new javafx.scene.control.Alert(type);
    a.setTitle(title); a.setHeaderText(header); a.setContentText(content); a.showAndWait();
  }

  private void performForcedLogout() {
    ClientSession.clear(); NetworkClient.getInstance().removeListener(this);
    try {
      javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
      javafx.stage.Stage stage = (javafx.stage.Stage) ContentArea.getScene().getWindow();
      stage.setScene(new javafx.scene.Scene(root)); stage.centerOnScreen(); stage.show();
    } catch (Exception e) { LOGGER.error("Forced logout navigation failed", e); }
  }
}
