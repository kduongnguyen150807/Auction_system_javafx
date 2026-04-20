package com.auction.client.util;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificationCenter {
  private static final ObservableList<String> notifications = FXCollections.observableArrayList();

  public static void addNotification(String message) {
    Platform.runLater(() -> notifications.add(0, message));
    try {
      if (SystemTray.isSupported()) {
        SystemTray tray = SystemTray.getSystemTray();
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        TrayIcon trayIcon = new TrayIcon(image, "Auction");
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);
        trayIcon.displayMessage("BÁO ĐỘNG ĐẤU GIÁ", message, TrayIcon.MessageType.WARNING);
        new Thread(
                () -> {
                  try {
                    Thread.sleep(5000);
                    tray.remove(trayIcon);
                  } catch (Exception e) {
                  }
                })
            .start();
      }
    } catch (Exception e) {
    }
  }

  public static ObservableList<String> getNotifications() {
    return notifications;
  }
}
