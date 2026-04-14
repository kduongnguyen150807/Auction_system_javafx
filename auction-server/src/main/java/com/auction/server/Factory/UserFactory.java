package com.auction.server.Factory;

import com.auction.shared.*;

public class UserFactory {
    public static User create(String role) {
        if (role == null) return new Bidder();
        return switch (role.toUpperCase()) {
            case "ADMIN" -> new Admin();
            case "SELLER" -> new Seller();
            default -> new Bidder();
        };
    }
}