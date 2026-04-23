package com.auction.server.service;

import com.auction.shared.User;
import com.auction.shared.UserRole;
import java.util.List;
import java.util.Map;

public class AdminService {
    private final UserService userService;
    private final ItemQueryService itemQueryService;

    public AdminService() {
        this.userService = new UserService();
        this.itemQueryService = new ItemQueryService();
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public boolean lockUser(String username) {
        return userService.setUserLocked(username, true);
    }

    public boolean unlockUser(String username) {
        return userService.setUserLocked(username, false);
    }

    public boolean setUserRole(String username, String role) {
        return userService.setUserRole(username, role);
    }

    public java.util.List<com.auction.shared.Item> getPendingItems() {
        return itemQueryService.getPendingItems();
    }

    public boolean approveItem(int id) {
        return itemQueryService.approveItem(id);
    }

    public boolean rejectItem(int id) {
        return itemQueryService.rejectItem(id);
    }

    public Map<String, Integer> getStatusStats() {
        return itemQueryService.getStatusStats();
    }

    public Map<String, Double> getCategoryStats() {
        return itemQueryService.getCategoryStats();
    }
}