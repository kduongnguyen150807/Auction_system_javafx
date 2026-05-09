package com.auction.client.ui.maindashboard.admin;

import com.auction.client.ui.base.CanRefresh;

public class AnalyticsController implements CanRefresh {
  @Override
  public void refreshData() {
    System.out.println("hello");
  }
}
