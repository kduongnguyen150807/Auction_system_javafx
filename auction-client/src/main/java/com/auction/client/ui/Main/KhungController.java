package com.auction.client.ui.Main;
import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.AddNewLot.AddNewLotController;
import com.auction.client.ui.TrangChu.TrangChuController;
import com.auction.client.ui.YourItem.YourItemController;
import com.auction.shared.UserRole;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
public class KhungController {
private static KhungController instance;
private static Pane khungChua;
private static Node currentNode;
private static String searchKeyword = "";
private static String categoryFilter = "All";
private Node auctionNode;
private Node historyNode;
private Node myItemNode;
private Node profileNode;
private Node adminNode;
private Node addlotnode;
private Node nodeBeforeAddLot;
private HBox menuBeforeAddLot;
private TrangChuController trangChuController;
private YourItemController yourItemController;
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
@FXML private javafx.scene.image.ImageView sidebaravatar;
@FXML
public void initialize() {
instance = this;
khungChua = ContentArea;
try {
NodeContentLoader<HBox> searchLoader = new NodeContentLoader<>();
searchLoader.load("/fxml/searchbar/ThanhTimKiem.fxml");
NodeContentLoader<ScrollPane> auctionLoader = new NodeContentLoader<>();
auctionLoader.load("/fxml/trangchu/TrangChu.fxml");
NodeContentLoader<ScrollPane> historyLoader = new NodeContentLoader<>();
historyLoader.load("/fxml/history/History.fxml");
NodeContentLoader<ScrollPane> myItemLoader = new NodeContentLoader<>();
myItemLoader.load("/fxml/youritem/YourItem.fxml");
NodeContentLoader<ScrollPane> profileLoader = new NodeContentLoader<>();
profileLoader.load("/fxml/profile/Profile.fxml");
NodeContentLoader<VBox> adminLoader = new NodeContentLoader<>();
adminLoader.load("/fxml/main/AdminDashboard.fxml");
NodeContentLoader<AnchorPane> addlotloader = new NodeContentLoader<>();
addlotloader.load("/fxml/addnewlot/AddNewLot.fxml");
NodeManager.addNodeToPane(auctionLoader, ContentArea);
NodeManager.addNodeToPane(searchLoader, SearchContainer);
auctionNode = auctionLoader.getCurrentNode();
historyNode = historyLoader.getCurrentNode();
myItemNode = myItemLoader.getCurrentNode();
profileNode = profileLoader.getCurrentNode();
adminNode = adminLoader.getCurrentNode();
addlotnode = addlotloader.getCurrentNode();
trangChuController = auctionLoader.getController();
yourItemController = myItemLoader.getController();
currentNode = auctionNode;
setActiveMenu(AuctionMenu);
applySessionToSidebar();
} catch (IOException ignored) {
}
}
@FXML
public void openAuction(MouseEvent e) {
switchContent(auctionNode);
setActiveMenu(AuctionMenu);
}
@FXML
public void openHistory(MouseEvent e) {
switchContent(historyNode);
setActiveMenu(HistoryMenu);
}
@FXML
public void openMyItems(MouseEvent e) {
if (ClientSession.getActiveRole() != UserRole.SELLER) return;
switchContent(myItemNode);
setActiveMenu(MyItemMenu);
if (yourItemController != null) {
yourItemController.refreshItems();
}
}
@FXML
public void openProfile(MouseEvent e) {
switchContent(profileNode);
setActiveMenu(ProfileMenu);
}
@FXML
public void openManageUsers(MouseEvent e) {
switchContent(adminNode);
setActiveMenu(ManageUsersMenu);
}
@FXML
public void handleRefresh(ActionEvent e) {
if (trangChuController != null && currentNode == auctionNode) {
trangChuController.refreshItems();
} else if (yourItemController != null && currentNode == myItemNode) {
yourItemController.refreshItems();
}
}
@FXML
public void handleprimaryaction(ActionEvent e) {
UserRole ans = ClientSession.getActiveRole();
if (ans == UserRole.SELLER) {
nodeBeforeAddLot = currentNode;
menuBeforeAddLot = menuForNode(currentNode);
if (currentNode != addlotnode) {
AddNewLotController.resetWhenOpening();
}
switchContent(addlotnode);
} else {
switchContent(auctionNode);
setActiveMenu(AuctionMenu);
}
}
private HBox menuForNode(Node n) {
if (n == auctionNode) return AuctionMenu;
if (n == historyNode) return HistoryMenu;
if (n == myItemNode) return MyItemMenu;
if (n == profileNode) return ProfileMenu;
if (n == adminNode) return ManageUsersMenu;
return AuctionMenu;
}
private void switchContent(Node target) {
if (target == null || currentNode == null || target == currentNode) return;
NodeManager.switchNodewithNode(target, currentNode, ContentArea);
currentNode = target;
}
private void setActiveMenu(HBox active) {
AuctionMenu.getStyleClass().remove("active");
HistoryMenu.getStyleClass().remove("active");
MyItemMenu.getStyleClass().remove("active");
ProfileMenu.getStyleClass().remove("active");
ManageUsersMenu.getStyleClass().remove("active");
if (!active.getStyleClass().contains("active")) active.getStyleClass().add("active");
}
public static Pane getKhungChua() {
return khungChua;
}
public static Node getCurrentNode() {
return currentNode;
}
public static void refreshSidebarFromSession() {
if (instance != null) instance.applySessionToSidebar();
}
public static void applySearchFilter(String keyword, String category) {
searchKeyword = keyword == null ? "" : keyword.trim();
categoryFilter = (category == null || category.isBlank()) ? "All" : category.trim();
if (instance != null) {
if (instance.trangChuController != null) {
instance.trangChuController.setFilters(searchKeyword, categoryFilter);
}
if (instance.yourItemController != null) {
instance.yourItemController.setFilters(searchKeyword, categoryFilter);
}
}
}
public static String getSearchKeyword() {
return searchKeyword;
}
public static String getCategoryFilter() {
return categoryFilter;
}
public static void returnFromAddLot(boolean refreshAuction) {
if (instance == null) return;
Node back = instance.nodeBeforeAddLot != null ? instance.nodeBeforeAddLot : instance.auctionNode;
HBox menu = instance.menuBeforeAddLot != null ? instance.menuBeforeAddLot : instance.AuctionMenu;
instance.switchContent(back);
instance.setActiveMenu(menu);
if (refreshAuction && back == instance.auctionNode && instance.trangChuController != null) {
instance.trangChuController.refreshItems();
}
}
private void applySessionToSidebar() {
if (ClientSession.getCurrentUser() == null) return;
UserName.setText(fallback(ClientSession.getUsername(), "username"));
Rank.setText(toTitleCase(ClientSession.getActiveRole().name()));
UserRole ans = ClientSession.getCurrentUser().getrole();
boolean res = ans == UserRole.ADMIN;
ManageUsersMenu.setVisible(res);
ManageUsersMenu.setManaged(res);
UserRole roleans = ClientSession.getActiveRole();
if (primaryactionbutton != null) primaryactionbutton.setText(roleans == UserRole.SELLER ? "Create a New Auction" : "Place a Bid");
boolean showMyItems = roleans == UserRole.SELLER;
MyItemMenu.setVisible(showMyItems);
MyItemMenu.setManaged(showMyItems);
if (!showMyItems && currentNode == myItemNode) {
switchContent(auctionNode);
setActiveMenu(AuctionMenu);
}
String avatarres = ClientSession.getCurrentUser().getavatarurl();
if (avatarres != null && !avatarres.isBlank()) {
if (avatarres.contains(".webp")) avatarres = avatarres.replace(".webp", ".jpg");
final String avatarurl = avatarres;
javafx.application.Platform.runLater(() -> {
javafx.scene.image.Image avatarans = new javafx.scene.image.Image(avatarurl, true);
sidebaravatar.setImage(avatarans);
sidebaravatar.setClip(new javafx.scene.shape.Circle(24, 24, 24));
});
}
}
private String fallback(String value, String fallback) {
return (value == null || value.isBlank()) ? fallback : value;
}
private String toTitleCase(String value) {
if (value == null || value.isBlank()) return "Bidder";
String lower = value.toLowerCase();
return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
}
}
