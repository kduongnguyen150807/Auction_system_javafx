package com.auction.client.ui.Main;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.ui.History.HistoryController;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import com.auction.client.ui.TrangChu.TrangChuController;
import com.auction.client.ui.UserProfile.UserProfileController;
import com.auction.client.ui.YourItem.YourItemController;
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

public class KhungController {
  private static KhungController instance;
  private static Pane mainContentPane;
  private static Node currentContentNode;
  private static String searchKeyword = "";
  private static String categoryFilter = "All";
  private static double filterMinPrice = 0;
  private static double filterMaxPrice = Double.MAX_VALUE;
  public static ItemInformationController itemDetailController;

  private Node an, hn, mn, pn, adn, aln;
  private TrangChuController tc;
  private YourItemController yc;
  private HistoryController hc;

  @FXML private HBox SearchContainer;
  @FXML private StackPane ContentArea;
  @FXML private HBox AuctionMenu;
  @FXML private HBox HistoryMenu;
  @FXML private HBox MyItemMenu;
  @FXML private HBox ProfileMenu;
  @FXML private HBox ManageUsersMenu;
  @FXML private Label UserName;
  @FXML private Label Rank;
  @FXML private Button primaryactionbutton;
  @FXML private ImageView sidebaravatar;

  @FXML
  public void initialize() {
    instance = this;
    mainContentPane = ContentArea;
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
      update();

      Platform.runLater(
          () -> {
            javafx.scene.Scene ans3 = mainContentPane.getScene();
            if (ans3 != null) {
              ans3.addEventFilter(
                  javafx.scene.input.KeyEvent.KEY_PRESSED,
                  res4 -> {
                    if (res4.getCode() == javafx.scene.input.KeyCode.F11) {
                      javafx.stage.Stage ans4 = (javafx.stage.Stage) ans3.getWindow();
                      ans4.setFullScreen(!ans4.isFullScreen());
                      res4.consume();
                    }
                  });
            }
          });
    } catch (Exception e) {
    }
  }

  @FXML
  public void openAuction(MouseEvent e) {
    switchPage(an, AuctionMenu);
  }

  @FXML
  public void openHistory(MouseEvent e) {
    switchPage(hn, HistoryMenu);
    if (hc != null) hc.refreshHistory();
  }

  @FXML
  public void openMyItems(MouseEvent e) {
    switchPage(mn, MyItemMenu);
  }

  @FXML
  public void openProfile(MouseEvent e) {
    switchPage(pn, ProfileMenu);
  }

  @FXML
  public void openManageUsers(MouseEvent e) {
    switchPage(adn, ManageUsersMenu);
  }

  @FXML
  public void handleRefresh(ActionEvent e) {
    if (currentContentNode == an && tc != null) tc.refreshItems();
    else if (currentContentNode == hn && hc != null) hc.refreshHistory();
    else if (currentContentNode == mn && yc != null) yc.refreshItems();
    else if (itemDetailController != null) itemDetailController.refresh();
  }

  @FXML
  public void handlePrimaryAction(ActionEvent e) {
    UserRole ans = ClientSession.getActiveRole();
    if (ans == UserRole.SELLER) {
      if (currentContentNode != aln) {
        com.auction.client.ui.AddNewLot.AddNewLotController.resetWhenOpening();
      }
      switchPage(aln, AuctionMenu);
    } else {
      switchPage(an, AuctionMenu);
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
    if (a != null && !a.getStyleClass().contains("active")) a.getStyleClass().add("active");
  }

  public void update() {
    if (ClientSession.getCurrentUser() == null) return;
    if (UserName != null) UserName.setText(ClientSession.getUsername());
    if (Rank != null) {
      String ans = ClientSession.getActiveRole().name();
      User res = ClientSession.getCurrentUser();
      if (res.getTotalRatings() > 0) {
        String ans1 =
            res.getAvgRating() <= 2.0
                ? "Negative"
                : (res.getAvgRating() <= 3.0 ? "Neutral" : "Positive");
        ans = ans + " | " + String.format("%.1f\u2605 %s", res.getAvgRating(), ans1);
      }
      Rank.setText(ans);
    }
    boolean ans1 = ClientSession.getCurrentUser().getRole() == UserRole.ADMIN;
    if (ManageUsersMenu != null) {
      ManageUsersMenu.setVisible(ans1);
      ManageUsersMenu.setManaged(ans1);
    }
    String res1 = ClientSession.getCurrentUser().getAvatarUrl();
    if (sidebaravatar != null && res1 != null && !res1.isBlank()) {
      Image ans2 = new Image(res1, true);
      ans2.progressProperty()
          .addListener(
              (obs, oldv, newv) -> {
                if (newv.doubleValue() == 1.0 && !ans2.isError()) {
                  double res2 = ans2.getWidth();
                  double ans3 = ans2.getHeight();
                  double res3 = Math.min(res2, ans3);
                  double ans4 = (res2 - res3) / 2;
                  double res4 = (ans3 - res3) / 2;
                  Platform.runLater(
                      () -> {
                        sidebaravatar.setImage(ans2);
                        sidebaravatar.setViewport(
                            new javafx.geometry.Rectangle2D(ans4, res4, res3, res3));
                        sidebaravatar.setFitWidth(48);
                        sidebaravatar.setFitHeight(48);
                        sidebaravatar.setPreserveRatio(false);
                        double ans5 = 24.0;
                        sidebaravatar.setClip(new Circle(ans5, ans5, ans5));
                      });
                }
              });
    }
  }

  public static Pane getMainContentPane() {
    return mainContentPane;
  }

  public static Node getCurrentNode() {
    return currentContentNode;
  }

  public static void setMainContentNode(Node n) {
    if (n != null) currentContentNode = n;
  }

  public static void refreshSidebarFromSession() {
    if (instance != null) instance.update();
  }

  public static void applySearchFilter(String k, String c, double min, double max) {
    searchKeyword = k == null ? "" : k.trim();
    categoryFilter = c == null ? "All" : c.trim();
    filterMinPrice = min;
    filterMaxPrice = max;
    if (instance != null) {
      if (instance.tc != null) instance.tc.setFilters(searchKeyword, categoryFilter);
      if (instance.yc != null) instance.yc.refreshItems();
    }
  }

  public static void updateRealtimeUi(Item ans) {
    Platform.runLater(
        () -> {
          if (itemDetailController != null) {
            itemDetailController.updatePriceUi(ans);
          }
          if (instance != null && instance.tc != null) {
            instance.tc.updatePriceUi(ans);
          }
        });
  }

  public static String getSearchKeyword() {
    return searchKeyword;
  }

  public static String getCategoryFilter() {
    return categoryFilter;
  }

  public static double getMinPrice() {
    return filterMinPrice;
  }

  public static double getMaxPrice() {
    return filterMaxPrice;
  }

  public static void returnFromAddLot(boolean r) {
    if (instance == null) return;
    instance.switchPage(instance.an, instance.AuctionMenu);
    if (r && instance.tc != null) instance.tc.refreshItems();
  }

  public static void showUserProfile(User user) {
    if (instance == null || user == null) return;
    Platform.runLater(
        () -> {
          try {
            NodeContentLoader<javafx.scene.Parent> ans = new NodeContentLoader<>();
            ans.load("/fxml/userprofile/UserProfile.fxml");
            UserProfileController res = ans.getController();
            res.setUser(user);
            javafx.scene.Node ans1 = ans.getCurrentNode();
            if (instance.ContentArea != null) {
              instance.ContentArea.getChildren().clear();
              instance.ContentArea.getChildren().add(ans1);
            }
            currentContentNode = ans1;
            instance.setMenu(null);
          } catch (Exception e) {
          }
        });
  }

  public static void returnToAuction() {
    if (instance == null) return;
    Platform.runLater(() -> instance.switchPage(instance.an, instance.AuctionMenu));
  }
}
