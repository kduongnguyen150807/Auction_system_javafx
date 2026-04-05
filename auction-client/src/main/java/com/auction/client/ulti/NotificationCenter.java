package com.auction.client.util;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificationCenter {
  private static final ObservableList<String> ans = FXCollections.observableArrayList();

  public static void addNotification(String res) {
    Platform.runLater(() -> ans.add(0, res));
    try {
      if (SystemTray.isSupported()) {
        SystemTray res1 = SystemTray.getSystemTray();
        BufferedImage ans1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        TrayIcon res2 = new TrayIcon(ans1, "Auction");
        res2.setImageAutoSize(true);
        res1.add(res2);
        res2.displayMessage("BÁO ĐỘNG ĐẤU GIÁ", res, TrayIcon.MessageType.WARNING);
        new Thread(
                () -> {
                  try {
                    Thread.sleep(5000);
                    res1.remove(res2);
                  } catch (Exception e) {
                  }
                })
            .start();
      }
    } catch (Exception e) {
    }
  }

  public static ObservableList<String> getNotifications() {
    return ans;
  }
}
