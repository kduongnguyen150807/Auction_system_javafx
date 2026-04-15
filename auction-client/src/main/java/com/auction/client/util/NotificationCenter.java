package com.auction.client.util;

import com.auction.client.network.NetworkClient;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificationCenter {
  private static final ObservableList<String> ans = FXCollections.observableArrayList();
  private static final Map<Integer, String> itemNamesById = new ConcurrentHashMap<>();

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

  public static void cacheItem(Item res) {
    if (res == null) return;
    String ans = safeName(res.getName());
    if (ans == null) return;
    itemNamesById.put(res.getId(), ans);
  }

  public static void addOutbidNotification(Object res) {
    int ans = extractItemId(res);
    if (ans <= 0) {
      addNotification("🔥 BÁO ĐỘNG: Bạn vừa bị đè giá ở một sản phẩm!");
      return;
    }

    String res1 = itemNamesById.get(ans);
    if (res1 != null) {
      addNotification(formatOutbidMessage(res1));
      return;
    }

    Thread ans1 =
        new Thread(
            () -> {
              String res2 = fetchAndCacheItemName(ans);
              addNotification(formatOutbidMessage(res2 != null ? res2 : "một sản phẩm"));
            });
    ans1.setDaemon(true);
    ans1.start();
  }

  private static String fetchAndCacheItemName(int res) {
    try {
      Response ans = NetworkClient.getInstance().sendRequestAndWait(new Request(Request.GET_ITEM_BY_ID, res));
      if (ans == null || !Response.OK.equals(ans.getStatus())) return null;
      Object ans1 = ans.getPayload();
      if (!(ans1 instanceof Item)) return null;
      Item res1 = (Item) ans1;
      String res2 = safeName(res1.getName());
      if (res2 == null) return null;
      itemNamesById.put(res, res2);
      return res2;
    } catch (Exception e) {
      return null;
    }
  }

  private static int extractItemId(Object res) {
    if (res instanceof Item) {
      Item ans = (Item) res;
      cacheItem(ans);
      return ans.getId();
    }
    if (res instanceof Number) {
      Number ans = (Number) res;
      return ans.intValue();
    }
    if (res instanceof String) {
      try {
        return Integer.parseInt(((String) res).trim());
      } catch (Exception e) {
        return -1;
      }
    }
    return -1;
  }

  private static String formatOutbidMessage(String res) {
    return "🔥 BÁO ĐỘNG: Sản phẩm \"" + res + "\" bị đè giá rồi!";
  }

  private static String safeName(String res) {
    if (res == null) return null;
    String ans = res.trim();
    if (ans.isEmpty()) return null;
    return ans;
  }
}
