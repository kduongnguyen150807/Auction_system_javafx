package com.auction.client.ui.ItemInformation;

import com.auction.shared.AuctionType;
import com.auction.shared.DutchAuctionPricing;
import com.auction.shared.Item;
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
    LocalDateTime start = item.getStartTime();
    if (start != null && start.isAfter(now)) {
      endsInValue.setText("Starts in " + DutchAuctionPricing.formatShortCountdownToward(start, now));
      return;
    }
    LocalDateTime target =
        item.getAuctionType() == AuctionType.DUTCH
            ? DutchAuctionPricing.countdownTarget(item, now)
            : item.getEndTime();
    endsInValue.setText(DutchAuctionPricing.formatShortCountdownToward(target, now));
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
      Button bidButton,
      Button joinLiveButton) {
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
      boolean hideAuto = listingKind == AuctionType.DUTCH || listingKind == AuctionType.LIVE;
      autoBidHBox.setVisible(!hideAuto);
      autoBidHBox.setManaged(!hideAuto);
    }
    boolean isLive = listingKind == AuctionType.LIVE;
    if (bidButton != null && !bidButton.isDisable()) {
      bidButton.setVisible(!isLive);
      bidButton.setManaged(!isLive);
      if (!isLive) {
        bidButton.setText(
            listingKind == AuctionType.DUTCH ? "BUY AT CURRENT PRICE" : "PLACE BID NOW");
      }
    }
    if (joinLiveButton != null) {
      joinLiveButton.setVisible(isLive);
      joinLiveButton.setManaged(isLive);
      joinLiveButton.setText("VÀO LIVE");
    }
  }

  static void applyAuctionPresentation(
      AuctionType listingKind,
      Label bidMetricCaption,
      VBox buyItNowVBox,
      HBox autoBidHBox,
      Button bidButton) {
    applyAuctionPresentation(listingKind, bidMetricCaption, buyItNowVBox, autoBidHBox, bidButton, null);
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
    setBidButtonClosed(
        listingKind, bidButton, autobidfield, autobidbutton, null, auctionClosed, biddingNotYetStarted);
  }

  static void setBidButtonClosed(
      AuctionType listingKind,
      Button bidButton,
      TextField autobidfield,
      Button autobidbutton,
      Button joinLiveButton,
      boolean auctionClosed,
      boolean biddingNotYetStarted) {
    boolean isLive = listingKind == AuctionType.LIVE;
    boolean disabled = auctionClosed || biddingNotYetStarted;

    if (joinLiveButton != null && isLive) {
      joinLiveButton.setDisable(disabled);
      joinLiveButton.setText(
          auctionClosed ? "LIVE ĐÃ KẾT THÚC" : biddingNotYetStarted ? "CHƯA BẮT ĐẦU" : "VÀO LIVE");
      joinLiveButton.setStyle(
          disabled
              ? "-fx-background-color: #555555; -fx-text-fill: #999999; -fx-cursor: default;"
              : "");
    }

    if (bidButton == null || isLive) {
      if (autobidbutton != null) {
        autobidbutton.setDisable(disabled);
      }
      if (autobidfield != null) {
        autobidfield.setDisable(disabled);
      }
      return;
    }
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
