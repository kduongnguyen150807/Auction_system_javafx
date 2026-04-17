package com.auction.server.command;

import com.auction.shared.User;

public interface Authorizable {
    default boolean isOwner(User currentUser, int ownerId) {
        if (currentUser == null) return false;
        return currentUser.getId() == ownerId || "ADMIN".equals(currentUser.getRole().name());
    }

    default boolean isAdmin(User user){
        if(user == null) return false;
        return user.getRole().name().equals("ADMIN");
    }

    default boolean isAccountActive(User currentUser) {
        return currentUser != null && currentUser.isActive() && !currentUser.isLocked();
    }
}