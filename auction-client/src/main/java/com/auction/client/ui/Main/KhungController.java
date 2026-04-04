package com.auction.client.ui.Main;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.ui.TrangChu.TrangChuController;
import com.auction.client.ui.YourItem.YourItemController;
import com.auction.client.ui.History.HistoryController;
import com.auction.client.ui.ItemInformation.ItemInformationController;
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
    private static Pane kc;
    private static Node cn;
    private static String sk = "";
    private static String cf = "All";
    public static ItemInformationController infoc;

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
        kc = ContentArea;
        try {
            NodeContentLoader<HBox> sl = new NodeContentLoader<>();
            sl.load("/fxml/searchbar/ThanhTimKiem.fxml");
            NodeContentLoader<Pane> al = new NodeContentLoader<>();
            al.load("/fxml/trangchu/TrangChu.fxml");
            NodeContentLoader<Pane> hl = new NodeContentLoader<>();
            hl.load("/fxml/history/History.fxml");
            NodeContentLoader<Pane> ml = new NodeContentLoader<>();
            ml.load("/fxml/youritem/YourItem.fxml");
            NodeContentLoader<Pane> pl = new NodeContentLoader<>();
            pl.load("/fxml/profile/Profile.fxml");
            NodeContentLoader<Pane> adl = new NodeContentLoader<>();
            adl.load("/fxml/main/AdminDashboard.fxml");
            NodeContentLoader<Pane> all = new NodeContentLoader<>();
            all.load("/fxml/addnewlot/AddNewLot.fxml");

            if (ContentArea != null) ContentArea.getChildren().add(al.getCurrentNode());
            if (SearchContainer != null) SearchContainer.getChildren().add(sl.getCurrentNode());

            an = al.getCurrentNode();
            hn = hl.getCurrentNode();
            mn = ml.getCurrentNode();
            pn = pl.getCurrentNode();
            adn = adl.getCurrentNode();
            aln = all.getCurrentNode();

            tc = al.getController();
            yc = ml.getController();
            hc = hl.getController();

            cn = an;
            setmenu(AuctionMenu);
            update();
        } catch (Exception e) {}
    }

    @FXML public void openAuction(MouseEvent e) { switchpage(an, AuctionMenu); }
    @FXML public void openHistory(MouseEvent e) { switchpage(hn, HistoryMenu); if (hc != null) hc.refreshhistory(); }
    // Tìm hàm này và sửa lại:
    @FXML
    public void openMyItems(MouseEvent e) {
        switchpage(mn, MyItemMenu);
    }
    @FXML public void openProfile(MouseEvent e) { switchpage(pn, ProfileMenu); }
    @FXML public void openManageUsers(MouseEvent e) { switchpage(adn, ManageUsersMenu); }

    @FXML
    public void handleRefresh(ActionEvent e) {
        if (cn == an && tc != null) tc.refreshItems();
        else if (cn == hn && hc != null) hc.refreshhistory();
        else if (cn == mn && yc != null) yc.refreshItems();
        else if (infoc != null) infoc.refresh();
    }

    @FXML
    public void handleprimaryaction(ActionEvent e) {
        UserRole ans = ClientSession.getActiveRole();
        if (ans == UserRole.SELLER) {
            if (cn != aln) {
                com.auction.client.ui.AddNewLot.AddNewLotController.resetWhenOpening();
            }
            switchpage(aln, AuctionMenu);
        } else {
            switchpage(an, AuctionMenu);
        }
    }

    private void switchpage(Node t, HBox m) {
        if (t == null || cn == t) return;
        if (ContentArea != null) {
            ContentArea.getChildren().clear();
            ContentArea.getChildren().add(t);
        }
        cn = t;
        setmenu(m);
    }

    private void setmenu(HBox a) {
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
            String res = ClientSession.getActiveRole().name();
            User res1 = ClientSession.getCurrentUser();
            if (res1.gettotalratings() > 0) {
                String res2 = res1.getavgrating() <= 2.0 ? "Negative" : (res1.getavgrating() <= 3.0 ? "Neutral" : "Positive");
                res = res + " | " + String.format("%.1f\u2605 %s", res1.getavgrating(), res2);
            }
            Rank.setText(res);
        }
        boolean isadmin = ClientSession.getCurrentUser().getrole() == UserRole.ADMIN;
        if (ManageUsersMenu != null) {
            ManageUsersMenu.setVisible(isadmin);
            ManageUsersMenu.setManaged(isadmin);
        }
        String u = ClientSession.getCurrentUser().getavatarurl();
        if (sidebaravatar != null && u != null && !u.isBlank()) {
            Image ansimg = new Image(u, true);
            ansimg.progressProperty().addListener((obs, oldv, newv) -> {
                if (newv.doubleValue() == 1.0 && !ansimg.isError()) {
                    double resw = ansimg.getWidth();
                    double resh = ansimg.getHeight();
                    double ansmin = Math.min(resw, resh);
                    double resx = (resw - ansmin) / 2;
                    double resy = (resh - ansmin) / 2;
                    Platform.runLater(() -> {
                        sidebaravatar.setImage(ansimg);
                        sidebaravatar.setViewport(new javafx.geometry.Rectangle2D(resx, resy, ansmin, ansmin));
                        sidebaravatar.setFitWidth(48);
                        sidebaravatar.setFitHeight(48);
                        sidebaravatar.setPreserveRatio(false);
                        double ansr = 24.0;
                        sidebaravatar.setClip(new Circle(ansr, ansr, ansr));
                    });
                }
            });
        }
    }

    public static Pane getKhungChua() { return kc; }
    public static Node getCurrentNode() { return cn; }
    public static void setMainContentNode(Node n) { if (n != null) cn = n; }
    public static void refreshSidebarFromSession() { if (instance != null) instance.update(); }

    public static void applySearchFilter(String k, String c) {
        sk = k == null ? "" : k.trim();
        cf = c == null ? "All" : c.trim();
        if (instance != null) {
            if (instance.tc != null) instance.tc.setFilters(sk, cf);
            if (instance.yc != null) instance.yc.setFilters(sk, cf);
        }
    }

    public static void updaterealtimeui(Item res) {
        Platform.runLater(() -> {
            if (infoc != null) {
                infoc.updatepriceui(res);
            }
            if (instance != null && instance.tc != null) {
                instance.tc.updatepriceui(res);
            }
        });
    }

    public static String getSearchKeyword() { return sk; }
    public static String getCategoryFilter() { return cf; }

    public static void returnFromAddLot(boolean r) {
        if (instance == null) return;
        instance.switchpage(instance.an, instance.AuctionMenu);
        if (r && instance.tc != null) instance.tc.refreshItems();
    }
}