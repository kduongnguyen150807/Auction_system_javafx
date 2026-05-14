package com.auction.client.ui.Main;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/** Swaps main {@link StackPane} content and sidebar menu highlight state. */
final class MainShellNavigator {

  private final StackPane contentArea;
  private final HBox auctionMenu;
  private final HBox watchlistMenu; // ĐÃ THÊM NÚT WATCHLIST
  private final HBox historyMenu;
  private final HBox myItemMenu;
  private final HBox profileMenu;
  private final HBox chatMenu;
  private final HBox manageUsersMenu;

  private Node currentContentNode;

  MainShellNavigator(
          StackPane contentArea,
          HBox auctionMenu,
          HBox watchlistMenu, // ĐÃ THÊM NÚT WATCHLIST
          HBox historyMenu,
          HBox myItemMenu,
          HBox profileMenu,
          HBox chatMenu,
          HBox manageUsersMenu,
          Node initialContent) {
    this.contentArea = contentArea;
    this.auctionMenu = auctionMenu;
    this.watchlistMenu = watchlistMenu; // ĐÃ THÊM NÚT WATCHLIST
    this.historyMenu = historyMenu;
    this.myItemMenu = myItemMenu;
    this.profileMenu = profileMenu;
    this.chatMenu = chatMenu;
    this.manageUsersMenu = manageUsersMenu;
    this.currentContentNode = initialContent;
  }

  Node getCurrentContentNode() {
    return currentContentNode;
  }

  void setCurrentContentNode(Node node) {
    if (node != null) currentContentNode = node;
  }

  /**
   * @return true if the visible root changed (callers may refresh remote data).
   */
  boolean switchPage(Node targetContent, HBox activeMenu) {
    if (targetContent == null || currentContentNode == targetContent) return false;
    if (contentArea != null) {
      contentArea.getChildren().clear();
      contentArea.getChildren().add(targetContent);
    }
    currentContentNode = targetContent;
    setMenu(activeMenu);
    return true;
  }

  /** Clears content area and shows {@code root}; clears menu highlight when {@code activeMenu} is null. */
  void replaceContent(Node root, HBox activeMenu) {
    if (contentArea != null && root != null) {
      contentArea.getChildren().clear();
      contentArea.getChildren().add(root);
    }
    currentContentNode = root;
    setMenu(activeMenu);
  }

  void setMenu(HBox active) {
    for (HBox m : menuBoxes()) if (m != null) m.getStyleClass().remove("active");
    if (active != null && !active.getStyleClass().contains("active")) active.getStyleClass().add("active");
  }

  private HBox[] menuBoxes() {
    // ĐÃ THÊM watchlistMenu VÀO MẢNG ĐỂ NÓ ĐỔI MÀU KHI CLICK
    return new HBox[] {auctionMenu, watchlistMenu, historyMenu, myItemMenu, profileMenu, chatMenu, manageUsersMenu};
  }
}