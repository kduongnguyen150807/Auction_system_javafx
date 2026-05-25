package com.auction.client.ui.homeview;

public enum HomeViewType {
  TRANG_CHU("/fxml/homeview/TrangChu.fxml"),
  PROFILE("/fxml/homeview/profile/Profile.fxml"),
  ADD_NEW_LOT("/fxml/homeview/AddNewLot.fxml"),
  ADMIN_DASHBOARD("/fxml/homeview/admin/AdminDashboard.fxml"),
  ITEM_INFORMATION("/fxml/homeview/iteminformation/ItemInformation.fxml"),
  RESULT_PAGE("/fxml/homeview/ResultPage.fxml"),
  HISTORY("/fxml/homeview/History.fxml"),
  MY_ITEM("/fxml/homeview/YourItem.fxml"),
  WATCHED_LIST("/fxml/homeview/WatchList.fxml"),
  USER_INFORMATION("/fxml/homeview/UserInformation.fxml"),
  CHAT_PAGE("/fxml/homeview/chat/ChatPage.fxml"),
  ;

  private String fxmlPath;

  HomeViewType(String fxmlPath) {
    this.fxmlPath = fxmlPath;
  }

  public String getFxmlPath() {
    return fxmlPath;
  }
}
