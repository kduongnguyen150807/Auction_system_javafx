package com.auction.client.ui.ItemInformation;

import com.auction.shared.AuctionType;
import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import java.time.LocalDateTime;
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
    LocalDateTime now = LocalDateTime.now();
    ItemStatus status = item.getStatus();
    if (status != null && status != ItemStatus.OPEN) {
      endsInValue.setText(closedEndsInCaption(status));
      return;
    }
    LocalDateTime start = item.getStartTime();
    if (start != null && start.isAfter(now)) {
      endsInValue.setText("Starts in " + DutchAuctionPricing.formatShortCountdownToward(start, now));
      return;
    }
    LocalDateTime endTime = item.getEndTime();
    if (endTime != null && !endTime.isAfter(now)) {
      endsInValue.setText("Auction Closed");
      return;
    }
    LocalDateTime target =
        item.getAuctionType() == AuctionType.DUTCH
            ? DutchAuctionPricing.countdownTarget(item, now)
            : endTime;
    endsInValue.setText(DutchAuctionPricing.formatShortCountdownToward(target, now));
  }

  /** Updates current listed price for Dutch auctions on each UI tick. */
  static void applyDutchLivePrice(Label priceLabel, Item item) {
    if (priceLabel == null || item == null || item.getAuctionType() != AuctionType.DUTCH) {
      return;
    }
    if (item.getStatus() != ItemStatus.OPEN) {
      return;
    }
    double effective = DutchAuctionPricing.computeEffectivePrice(item, LocalDateTime.now());
    item.setCurrentPrice(effective);
    priceLabel.setText(formatPriceDollars(effective));
  }

  private static String closedEndsInCaption(ItemStatus status) {
    return switch (status) {
      case EXPIRED -> "Expired";
      case CANCELED -> "Canceled";
      case PENDING -> "Pending approval";
      default -> "Auction Closed";
    };
  }

  /** True when lot is OPEN in DB but {@code startTime} is still in the future. */
  static boolean isAuctionUpcoming(Item item, LocalDateTime startTimeFallback) {
    LocalDateTime start = item != null && item.getStartTime() != null ? item.getStartTime() : startTimeFallback;
    if (start == null) {
      return false;
    }
    return LocalDateTime.now().isBefore(start);
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

  /**
   * @param auctionClosed finalized state (sold / canceled / not OPEN)
   * @param biddingNotYetStarted OPEN but {@code startTime} not reached
   */
  static void setBidButtonClosed(
      AuctionType listingKind,
      Button bidButton,
      TextField autobidfield,
      Button autobidbutton,
      boolean auctionClosed,
      boolean biddingNotYetStarted) {
    if (bidButton == null) {
      return;
    }
    boolean disabled = auctionClosed || biddingNotYetStarted;
    String openCaption =
        listingKind == AuctionType.DUTCH ? "BUY AT CURRENT PRICE" : "PLACE BID NOW";
    String caption =
        auctionClosed ? "CLOSED" : biddingNotYetStarted ? "NOT STARTED YET" : openCaption;
    bidButton.setText(caption);
    bidButton.setDisable(disabled);
    bidButton.setStyle(
        disabled
            ? "-fx-background-color: #555555; -fx-text-fill: #999999; -fx-cursor: default;"
            : "");
    if (autobidbutton != null) {
      autobidbutton.setDisable(disabled);
    }
    if (autobidfield != null) {
      autobidfield.setDisable(disabled);
    }
  }
}
