package com.auction.client.ui.maindashboard;

enum HomeViewType {
  AUCTION("/fxml/MainDashBoard/Auction.fxml"),
  PROFILE("/fxml/MainDashBoard/Profile.fxml"),
  REGISTER_LOT("/fxml/MainDashBoard/RegisterLot.fxml"),
  ;

  private final String fxmlPath;

  HomeViewType(String fxmlPath) {
    this.fxmlPath = fxmlPath;
  }

  public String getFxmlPath() {
    return fxmlPath;
  }
}
