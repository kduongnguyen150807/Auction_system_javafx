package com.auction.client.util;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificationCenter {
  private static final ObservableList<String> notifications = FXCollections.observableArrayList();

  /**
   * Single reusable tray icon — created once and never re-added to the tray.
   * Previously a new TrayIcon was added on every call, causing duplicate OS
   * popups when several notifications arrived in quick succession.
   */
  private static TrayIcon trayIcon;

  private static synchronized TrayIcon getTrayIcon() {
    if (trayIcon != null) {
      return trayIcon;
    }
    try {
      if (SystemTray.isSupported()) {
        SystemTray tray = SystemTray.getSystemTray();
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        trayIcon = new TrayIcon(image, "Auction");
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);
      }
    } catch (Exception ignored) {
    }
    return trayIcon;
  }

  public static void addNotification(String message) {
    Platform.runLater(() -> notifications.add(0, message));
    try {
      TrayIcon icon = getTrayIcon();
      if (icon != null) {
        icon.displayMessage("BÁO ĐỘNG ĐẤU GIÁ", message, TrayIcon.MessageType.WARNING);
      }
    } catch (Exception ignored) {
    }
  }

  public static ObservableList<String> getNotifications() {
    return notifications;
  }
}
