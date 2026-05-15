package com.auction.client;

import com.auction.shared.User;
import com.auction.shared.UserRole;

/**
 * Holds the currently authenticated user's state for the lifetime of the
 * session. This is a pure value-holder: it contains NO network or business
 * logic. All mutations that require a server round-trip must go through
 * {@link com.auction.client.service.UserAccountService}.
 */
public final class ClientSession {
  private static User currentUser;
  private static UserRole activeRole;

  private ClientSession() {}

  public static void setCurrentUser(User user) {
    currentUser = user;
    if (user != null && activeRole == null) {
      activeRole = user.getRole() == null ? UserRole.BIDDER : user.getRole();
    }
  }

  /** Syncs profile fields on {@link #currentUser} after a successful profile update. */
  public static void applyProfileUpdate(String newFullName, String newEmail, String newPhone) {
    if (currentUser == null) return;
    currentUser.setFullName(safe(newFullName));
    currentUser.setEmail(safe(newEmail));
    currentUser.setPhoneNumber(safe(newPhone));
  }

  public static User getCurrentUser() {
    return currentUser;
  }

  public static String getUsername() {
    return currentUser == null ? "" : safe(currentUser.getUsername());
  }

  public static UserRole getActiveRole() {
    return activeRole;
  }

  public static void toggleRole() {
    if (activeRole == UserRole.SELLER) activeRole = UserRole.BIDDER;
    else activeRole = UserRole.SELLER;
  }

  public static void clear() {
    currentUser = null;
    activeRole = null;
  }
  // Cache lưu trữ ID các item đang theo dõi (O(1) lookup)
  private static final java.util.Set<Integer> watchedItemIds = java.util.concurrent.ConcurrentHashMap.newKeySet();

  public static boolean isWatching(int itemId) {
    return watchedItemIds.contains(itemId);
  }

  public static void toggleWatch(int itemId, boolean isWatching) {
    if (isWatching) watchedItemIds.add(itemId);
    else watchedItemIds.remove(itemId);
  }

  public static void setWatchlist(java.util.List<Integer> ids) {
    watchedItemIds.clear();
    if (ids != null) watchedItemIds.addAll(ids);
  }

  // Nhớ thêm watchedItemIds.clear(); vào trong hàm clear() (lúc logout)
  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
