package com.auction.client.ui.maindashboard;

import com.auction.client.ui.base.CanRefresh;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileController implements CanRefresh {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);



  @FXML
  private void initialize() {
    refreshData();
  }

  @Override
  public void refreshData() {
    LOGGER.info("Refreshing data");
  }

  @FXML
  private void handleChangeAvatar() {
    refreshData();
  }

  @FXML
  private void handleToggleRole() {

  }
}
