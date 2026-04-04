package com.auction.client.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;

public class NotificationCenter {
    private static final ObservableList<String> ans = FXCollections.observableArrayList();

    public static void addnotification(String res) {
        Platform.runLater(() -> ans.add(0, res));
    }

    public static ObservableList<String> getnotifications() {
        return ans;
    }
}