package com.auction.client.ui.utils;

public record ValidationResult(boolean isValid, String message) {
    public static ValidationResult ok() {
        return new ValidationResult(true, "");
    }
    public static ValidationResult fail(String message) {
        return new ValidationResult(false, message);
    }
}