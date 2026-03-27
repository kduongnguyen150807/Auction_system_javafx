package com.auction.client.ui.Main;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.TrangChu.TrangChuController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class KhungController {
    private static KhungController instance;
    private static Pane khungChua;
    private static Node currentNode;

    private Node auctionNode;
    private Node historyNode;
    private Node profileNode;
    private TrangChuController trangChuController;

    @FXML private HBox SearchContainer;
    @FXML private StackPane ContentArea;
    @FXML private HBox AuctionMenu;
    @FXML private HBox HistoryMenu;
    @FXML private HBox ProfileMenu;
    @FXML private Label UserName;
    @FXML private Label Rank;

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
            NodeContentLoader<ScrollPane> profileLoader = new NodeContentLoader<>();
            profileLoader.load("/fxml/profile/Profile.fxml");

            NodeManager.addNodeToPane(auctionLoader, ContentArea);
            NodeManager.addNodeToPane(searchLoader, SearchContainer);

            auctionNode = auctionLoader.getCurrentNode();
            historyNode = historyLoader.getCurrentNode();
            profileNode = profileLoader.getCurrentNode();
            trangChuController = auctionLoader.getController();
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
    public void openProfile(MouseEvent e) {
        switchContent(profileNode);
        setActiveMenu(ProfileMenu);
    }

    @FXML
    public void handleRefresh(ActionEvent e) {
        if (trangChuController != null && currentNode == auctionNode) {
            trangChuController.refreshItems();
        }
    }

    private void switchContent(Node target) {
        if (target == null || currentNode == null || target == currentNode) return;
        NodeManager.switchNodewithNode(target, currentNode, ContentArea);
        currentNode = target;
    }

    private void setActiveMenu(HBox active) {
        AuctionMenu.getStyleClass().remove("active");
        HistoryMenu.getStyleClass().remove("active");
        ProfileMenu.getStyleClass().remove("active");
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

    private void applySessionToSidebar() {
        if (ClientSession.getCurrentUser() == null) return;
        UserName.setText(fallback(ClientSession.getUsername(), "username"));
        Rank.setText(toTitleCase(ClientSession.getActiveRole().name()));
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
