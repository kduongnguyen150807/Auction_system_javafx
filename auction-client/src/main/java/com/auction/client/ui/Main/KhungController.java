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
import javafx.scene.Node;
<<<<<<< HEAD
import javafx.scene.control.Button;
=======
import javafx.scene.Parent;
import javafx.scene.Scene;
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
<<<<<<< HEAD
import javafx.scene.shape.Circle;
=======
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068

public class KhungController {
  private static final Logger LOGGER = LoggerFactory.getLogger(KhungController.class);
  private static KhungController instance;
  private static Pane mainContentPane;

  public static ItemInformationController itemDetailController;

<<<<<<< HEAD
  private Node chatNode;
  private com.auction.client.Chat.client.ChatController chatController;

  private Node an, hn, mn, pn, adn, aln, cn;
  private TrangChuController tc;
  private YourItemController yc;
  private HistoryController hc;
=======
  private final AuctionSearchFilterState searchFilters = new AuctionSearchFilterState();

  private MainShellNavigator navigator;
  private MainShellNetworkBridge networkBridge;

  private Node auctionHomeNode, historyNode, myItemsNode, profileNode, adminDashboardNode, addLotNode, chatNode;
  private TrangChuController homeController;
  private YourItemController myItemsController;
  private HistoryController historyController;
  private ProfileController profileController;
  private AdminDashboardController adminDashboardController;
  private ChatPageController chatCtrl;
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068

  @FXML private HBox MessageMenu;
  @FXML private HBox SearchContainer;
  @FXML private StackPane ContentArea;
  @FXML private HBox AuctionMenu, HistoryMenu, MyItemMenu, ProfileMenu, ChatMenu, ManageUsersMenu;
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
<<<<<<< HEAD
      NodeContentLoader<Pane> chatLoader = new NodeContentLoader<>();
      chatLoader.load("/fxml/GlobalChat/chat-view.fxml");
      chatNode = chatLoader.getCurrentNode();
      chatController = chatLoader.getController();

      if (ClientSession.getCurrentUser() != null) {
        String tenDangNhap = ClientSession.getCurrentUser().getUsername();
        chatController.initUserData(tenDangNhap);
      } else {
        chatController.initUserData("Khách Vãng Lai");
      }
      System.out.println("✅ Đã load FXML Chat thành công (Load đầu tiên)!");
    } catch (Exception e) {
      System.out.println("❌ LỖI KHI LOAD GIAO DIỆN CHAT:");
      e.printStackTrace();
    }
    try {
      NodeContentLoader<HBox> res = new NodeContentLoader<>();
      res.load("/fxml/searchbar/ThanhTimKiem.fxml");

      NodeContentLoader<Pane> ans = new NodeContentLoader<>();
      ans.load("/fxml/trangchu/TrangChu.fxml");

      NodeContentLoader<Pane> res1 = new NodeContentLoader<>();
      res1.load("/fxml/history/History.fxml");

      NodeContentLoader<Pane> ans1 = new NodeContentLoader<>();
      ans1.load("/fxml/youritem/YourItem.fxml");

      NodeContentLoader<Pane> res2 = new NodeContentLoader<>();
      res2.load("/fxml/profile/Profile.fxml");

      NodeContentLoader<Pane> ans2 = new NodeContentLoader<>();
      ans2.load("/fxml/main/AdminDashboard.fxml");

      NodeContentLoader<Pane> res3 = new NodeContentLoader<>();
      res3.load("/fxml/addnewlot/AddNewLot.fxml");

      if (ContentArea != null) ContentArea.getChildren().add(ans.getCurrentNode());
      if (SearchContainer != null) SearchContainer.getChildren().add(res.getCurrentNode());

      an = ans.getCurrentNode();
      hn = res1.getCurrentNode();
      mn = ans1.getCurrentNode();
      pn = res2.getCurrentNode();
      adn = ans2.getCurrentNode();
      aln = res3.getCurrentNode();

      tc = ans.getController();
      yc = ans1.getController();
      hc = res1.getController();

      currentContentNode = an;
      setMenu(AuctionMenu);
=======
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
      auctionHomeNode = auction.getCurrentNode();
      historyNode = hist.getCurrentNode();
      myItemsNode = myItem.getCurrentNode();
      profileNode = profile.getCurrentNode();
      adminDashboardNode = admin.getCurrentNode();
      addLotNode = addLot.getCurrentNode();
      chatNode = chat.getCurrentNode();
      homeController = auction.getController();
      myItemsController = myItem.getController();
      historyController = hist.getController();
      profileController = profile.getController();
      adminDashboardController = admin.getController();
      chatCtrl = chat.getController();
      navigator =
          new MainShellNavigator(
              ContentArea,
              AuctionMenu,
              HistoryMenu,
              MyItemMenu,
              ProfileMenu,
              ChatMenu,
              ManageUsersMenu,
              auctionHomeNode);
      navigator.setMenu(AuctionMenu);
      networkBridge = new MainShellNetworkBridge(() -> homeController, () -> chatCtrl);
      NetworkClient.getInstance().addListener(networkBridge);
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068
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
<<<<<<< HEAD
      System.out.println("⚠️ Lỗi khi load các màn hình chính (Do Server tắt hoặc mạng): " + e.getMessage());
=======
      LOGGER.warn("Main shell initialize failed", e);
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068
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

  @FXML
<<<<<<< HEAD
  public void openMessage(MouseEvent e) {
    System.out.println("Đã bấm nút Message! chatNode hiện tại là: " + chatNode);
    switchPage(chatNode, MessageMenu);

    if (chatController != null && ClientSession.getCurrentUser() != null) {
      chatController.initUserData(ClientSession.getUsername());
    }
  }

  @FXML
  public void openMyItems(MouseEvent e) {
    switchPage(mn, MyItemMenu);
=======
  public void openHistory(MouseEvent event) {
    if (switchPage(historyNode, HistoryMenu) && historyController != null) historyController.refreshHistory();
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068
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
<<<<<<< HEAD
    ClientSession.clear();
    try {
      javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
      javafx.stage.Stage stage = (javafx.stage.Stage) ContentArea.getScene().getWindow();
      javafx.scene.Scene scene = new javafx.scene.Scene(root);
      stage.setScene(scene);
      stage.centerOnScreen();
      stage.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void switchPage(Node t, HBox m) {
    if (t == null || currentContentNode == t) return;
    if (ContentArea != null) {
      ContentArea.getChildren().clear();
      ContentArea.getChildren().add(t);
    }
    currentContentNode = t;
    setMenu(m);
  }

  private void setMenu(HBox a) {
    if (AuctionMenu != null) AuctionMenu.getStyleClass().remove("active");
    if (HistoryMenu != null) HistoryMenu.getStyleClass().remove("active");
    if (MyItemMenu != null) MyItemMenu.getStyleClass().remove("active");
    if (ProfileMenu != null) ProfileMenu.getStyleClass().remove("active");
    if (ManageUsersMenu != null) ManageUsersMenu.getStyleClass().remove("active");
    if (MessageMenu != null) MessageMenu.getStyleClass().remove("active");

    if (a != null && !a.getStyleClass().contains("active")) a.getStyleClass().add("active");
=======
    performUserSignOut();
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068
  }

  public void update() {
    User currentUser = ClientSession.getCurrentUser();
    if (currentUser == null) return;
    if (UserName != null) UserName.setText(ClientSession.getUsername());
    if (Rank != null) {
      String rankText = ClientSession.getActiveRole().name();
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
  }

  public static void applySearchFilter(String keyword, String category, double min, double max) {
    if (instance == null) return;
    instance.searchFilters.apply(keyword, category, min, max);
    if (instance.homeController != null)
      instance.homeController.setFilters(instance.searchFilters.getKeyword(), instance.searchFilters.getCategory());
    if (instance.myItemsController != null) instance.myItemsController.refreshItems();
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
