package com.auction.client.ui.ItemInformation;

import com.auction.client.ClientSession;
import com.auction.client.service.BiddingClientService;
import com.auction.shared.AuctionType;
import com.auction.shared.BidTransaction;
import com.auction.shared.Response;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

final class ItemInformationAutoBidCoordinator {

  private ItemInformationAutoBidCoordinator() {}

  static void submitIfValid(
      int itemId,
      AuctionType listingKind,
      TextField autobidfield,
      BiddingClientService biddingClientService) {
    if (listingKind == AuctionType.DUTCH) {
      ItemInformationDialogs.show(
          Alert.AlertType.WARNING,
          "Not supported",
          "Automatic bidding is not available for Dutch auctions.");
      return;
    }
    if (ClientSession.getCurrentUser() == null) {
      return;
    }
    String raw =
        (autobidfield != null ? autobidfield.getText() : "")
            .replace("$", "")
            .replace(",", "")
            .trim();
    if (raw.isBlank()) {
      ItemInformationDialogs.show(
          Alert.AlertType.WARNING, "Invalid", "Enter your maximum auto-bid amount.");
      return;
    }
    try {
      double maxBid = Double.parseDouble(raw);
      if (maxBid <= 0) {
        ItemInformationDialogs.show(
            Alert.AlertType.WARNING, "Invalid", "Max auto-bid must be a positive number.");
        return;
      }
      BidTransaction bid =
          new BidTransaction(itemId, ClientSession.getCurrentUser().getId(), 0);
      bid.setMaxAutoBid(maxBid);
      bid.setAutoBid(true);
      Thread t =
          new Thread(
              () -> {
                Response res = biddingClientService.placeBid(bid);
                Platform.runLater(
                    () -> {
                      if (res != null && Response.OK.equals(res.getStatus())) {
                        if (autobidfield != null) {
                          autobidfield.clear();
                        }
                        ItemInformationDialogs.show(
                            Alert.AlertType.INFORMATION,
                            "Auto-Bid Active",
                            String.format("Auto-bid set! Will bid up to %,.0f$", maxBid));
                      } else {
                        ItemInformationDialogs.show(
                            Alert.AlertType.ERROR,
                            "Auto-Bid Failed",
                            res != null ? res.getMessage() : "Failed to set auto-bid.");
                      }
                    });
              });
      t.setDaemon(true);
      t.start();
    } catch (NumberFormatException e) {
      ItemInformationDialogs.show(Alert.AlertType.ERROR, "Invalid", "Please enter a valid number.");
    }
  }
}
