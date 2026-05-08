package com.auction.client.ui.ItemInformation;

import com.auction.shared.AuctionType;
import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Item;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Shared label / button presentation for the item detail pane. */
final class ItemInformationUiHelper {

  private ItemInformationUiHelper() {}

  static String formatPriceDollars(double price) {
    return String.format("%,.0f$", price);
  }

  static String formatBuyItNowLine(double buyItNowPrice) {
    return buyItNowPrice > 0 ? String.format("BUY IT NOW: %,.0f$", buyItNowPrice) : "NO BUY IT NOW";
  }

  static void applyEndsIn(Label endsInValue, Item item) {
    if (endsInValue == null || item == null) {
      return;
    }
    java.time.LocalDateTime now = java.time.LocalDateTime.now();
    java.time.LocalDateTime target =
        item.getAuctionType() == AuctionType.DUTCH
            ? DutchAuctionPricing.countdownTarget(item, now)
            : item.getEndTime();
    endsInValue.setText(DutchAuctionPricing.formatShortCountdownToward(target, now));
  }

  static void applyAuctionPresentation(
      AuctionType listingKind,
      Label bidMetricCaption,
      VBox buyItNowVBox,
      HBox autoBidHBox,
      Button bidButton) {
    if (bidMetricCaption != null) {
      bidMetricCaption.setText(
          listingKind == AuctionType.DUTCH ? "CURRENT PRICE" : "CURRENT BID");
    }
    if (buyItNowVBox != null) {
      boolean hideBin = listingKind == AuctionType.DUTCH;
      buyItNowVBox.setVisible(!hideBin);
      buyItNowVBox.setManaged(!hideBin);
    }
    if (autoBidHBox != null) {
      boolean hideAuto = listingKind == AuctionType.DUTCH;
      autoBidHBox.setVisible(!hideAuto);
      autoBidHBox.setManaged(!hideAuto);
    }
    if (bidButton != null && !bidButton.isDisable()) {
      bidButton.setText(
          listingKind == AuctionType.DUTCH ? "BUY AT CURRENT PRICE" : "PLACE BID NOW");
    }
  }

  static void setBidButtonClosed(
      AuctionType listingKind,
      Button bidButton,
      TextField autobidfield,
      Button autobidbutton,
      boolean closed) {
    if (bidButton == null) {
      return;
    }
    String openCaption =
        listingKind == AuctionType.DUTCH ? "BUY AT CURRENT PRICE" : "PLACE BID NOW";
    bidButton.setText(closed ? "CLOSED" : openCaption);
    bidButton.setDisable(closed);
    bidButton.setStyle(
        closed
            ? "-fx-background-color: #555555; -fx-text-fill: #999999; -fx-cursor: default;"
            : "");
    if (autobidbutton != null) {
      autobidbutton.setDisable(closed);
    }
    if (autobidfield != null) {
      autobidfield.setDisable(closed);
    }
  }
}
