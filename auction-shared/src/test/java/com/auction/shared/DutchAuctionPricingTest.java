package com.auction.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for Dutch auction countdown and effective listed price formulas. */
@DisplayName("DutchAuctionPricing")
class DutchAuctionPricingTest {

  private static Electronics dutchFloor(
      LocalDateTime start,
      LocalDateTime end,
      double ceiling,
      double reserve,
      double tick,
      int intervalMinutes,
      double currentPrice) {
    Electronics item = new Electronics("lot", "", ceiling, currentPrice, 1);
    item.setAuctionType(AuctionType.DUTCH);
    item.setStartTime(start);
    item.setEndTime(end);
    item.setDutchReservePrice(reserve);
    item.setDutchTickAmount(tick);
    item.setDutchTickIntervalMinutes(intervalMinutes);
    return item;
  }

  @Nested
  @DisplayName("maxDropTicks")
  class MaxDropTicks {

    @Test
    void nullItem_returnsZero() {
      assertEquals(0, DutchAuctionPricing.maxDropTicks(null));
    }

    @Test
    void englishAuction_returnsZero() {
      Electronics it = new Electronics("x", "", 100, 100, 1);
      it.setAuctionType(AuctionType.ENGLISH);
      assertEquals(0, DutchAuctionPricing.maxDropTicks(it));
    }

    @Test
    void zeroTick_returnsZero() {
      LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 12, 0);
      Electronics it = dutchFloor(t0, t0.plusHours(2), 100, 50, 0, 5, 100);
      assertEquals(0, DutchAuctionPricing.maxDropTicks(it));
    }

    @Test
    void ceilingAtOrBelowReserve_returnsZero() {
      LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 12, 0);
      Electronics it = dutchFloor(t0, t0.plusHours(2), 50, 50, 10, 5, 50);
      assertEquals(0, DutchAuctionPricing.maxDropTicks(it));
    }

    @Test
    void ceiling100Reserve50Tick10_expectsFiveTicks() {
      LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 12, 0);
      Electronics it = dutchFloor(t0, t0.plusHours(2), 100, 50, 10, 5, 100);
      assertEquals(5, DutchAuctionPricing.maxDropTicks(it));
    }

    @Test
    void partialLastTickUsesCeil() {
      LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 12, 0);
      Electronics it = dutchFloor(t0, t0.plusHours(2), 100, 79, 15, 5, 100);
      assertEquals(2, DutchAuctionPricing.maxDropTicks(it));
    }
  }

  @Nested
  @DisplayName("computeEffectivePrice")
  class EffectivePrice {

    @Test
    void nullItem_returnsZero() {
      assertEquals(0, DutchAuctionPricing.computeEffectivePrice(null, LocalDateTime.now()), 1e-9);
    }

    @Test
    void beforeStart_noDropsYet() {
      LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
      LocalDateTime now = start.minusMinutes(30);
      Electronics it = dutchFloor(start, start.plusDays(1), 200, 100, 25, 10, 200);
      assertEquals(200, DutchAuctionPricing.computeEffectivePrice(it, now), 1e-9);
    }

    @Test
    void afterOneInterval_oneTick() {
      LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
      LocalDateTime now = start.plusMinutes(10);
      Electronics it = dutchFloor(start, start.plusDays(1), 200, 100, 25, 10, 200);
      assertEquals(175, DutchAuctionPricing.computeEffectivePrice(it, now), 1e-9);
    }

    @Test
    void neverGoesBelowReserve() {
      LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
      LocalDateTime now = start.plusDays(7);
      Electronics it = dutchFloor(start, start.plusDays(8), 200, 150, 10, 1, 200);
      assertEquals(150, DutchAuctionPricing.computeEffectivePrice(it, now), 1e-9);
    }

    @Test
    void englishItem_returnsCurrentPrice() {
      LocalDateTime t = LocalDateTime.of(2026, 1, 1, 12, 0);
      Electronics it = new Electronics("e", "", 50, 77, 1);
      it.setAuctionType(AuctionType.ENGLISH);
      assertEquals(77, DutchAuctionPricing.computeEffectivePrice(it, t), 1e-9);
    }
  }

  @Nested
  @DisplayName("countdownTarget")
  class CountdownTarget {

    @Test
    void englishItem_returnsEndTime() {
      LocalDateTime end = LocalDateTime.of(2026, 3, 1, 18, 0);
      Electronics it = new Electronics("e", "", 10, 10, 1);
      it.setAuctionType(AuctionType.ENGLISH);
      it.setEndTime(end);
      assertEquals(end, DutchAuctionPricing.countdownTarget(it, LocalDateTime.of(2026, 2, 1, 12, 0)));
    }

    @Test
    void dutch_whenMaxTicksApplied_returnsAuctionEnd() {
      LocalDateTime start = LocalDateTime.of(2026, 4, 1, 12, 0);
      LocalDateTime end = start.plusHours(10);
      Electronics it =
          dutchFloor(start, end, 100, 70, 10, /* interval */ 5, 70);
      LocalDateTime now = start.plusMinutes(30);
      assertEquals(end, DutchAuctionPricing.countdownTarget(it, now));
    }

    @Test
    void dutch_returnsNextScheduledDrop_whenBeforeEnd() {
      LocalDateTime start = LocalDateTime.of(2026, 4, 1, 12, 0);
      LocalDateTime end = start.plusDays(1);
      Electronics it = dutchFloor(start, end, 200, 100, 50, 15, 200);
      LocalDateTime now = start.plusMinutes(10);
      assertEquals(start.plusMinutes(15), DutchAuctionPricing.countdownTarget(it, now));
    }

    @Test
    void dutch_whenNextDropAfterEnd_returnsEnd() {
      LocalDateTime start = LocalDateTime.of(2026, 5, 1, 22, 0);
      LocalDateTime end = start.plusMinutes(20);
      Electronics it =
          dutchFloor(start, end, 200, 10, /* large tick → one drop consumes window */ 100, 30, 200);
      LocalDateTime now = start.plusMinutes(5);
      LocalDateTime next = start.plusMinutes(30);
      assertTrue(next.isAfter(end));
      assertEquals(end, DutchAuctionPricing.countdownTarget(it, now));
    }
  }

  @Nested
  @DisplayName("validateDutchSchedule")
  class ValidateSchedule {

    @Test
    void endBeforeLastDrop_rejected() {
      LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
      LocalDateTime end = start.plusMinutes(30);
      String err =
          DutchAuctionPricing.validateDutchSchedule(start, end, 200, 100, 25, 10);
      assertEquals("end_time_too_early_for_dutch_drops", err);
    }

    @Test
    void endAtSuggestedDuration_accepted() {
      LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
      long mins = DutchAuctionPricing.minAuctionDurationMinutes(200, 100, 25, 10);
      LocalDateTime end = start.plusMinutes(mins);
      assertNull(DutchAuctionPricing.validateDutchSchedule(start, end, 200, 100, 25, 10));
    }

    @Test
    void suggestedEndTime_matchesMinDuration() {
      LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
      LocalDateTime suggested =
          DutchAuctionPricing.suggestedEndTime(start, 200, 100, 25, 10);
      assertEquals(start.plusMinutes(40), suggested);
    }

    @Test
    void derivedTickFromStartEndInterval_splitsEvenly() {
      LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
      LocalDateTime end = start.plusMinutes(40);
      assertEquals(4, DutchAuctionPricing.dropSlotsBetween(start, end, 10));
      assertEquals(25.0, DutchAuctionPricing.derivedTickAmount(start, end, 10, 200, 100), 1e-9);
      assertNull(
          DutchAuctionPricing.validateDutchScheduleFromInterval(start, end, 200, 100, 10));
    }

    @Test
    void derivedTick_windowTooShort_rejected() {
      LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
      LocalDateTime end = start.plusMinutes(5);
      assertEquals(-1, DutchAuctionPricing.derivedTickAmount(start, end, 10, 200, 100), 1e-9);
      assertEquals(
          "dutch_window_too_short",
          DutchAuctionPricing.validateDutchScheduleFromInterval(start, end, 200, 100, 10));
    }
  }

  @Nested
  @DisplayName("formatShortCountdownToward")
  class FormatCountdown {

    @Test
    void nullTarget_returnsNa() {
      assertEquals("N/A", DutchAuctionPricing.formatShortCountdownToward(null, LocalDateTime.now()));
    }

    @Test
    void zeroOrPast_returnsZeroSeconds() {
      LocalDateTime t = LocalDateTime.of(2026, 8, 1, 12, 0);
      assertEquals("0s", DutchAuctionPricing.formatShortCountdownToward(t, t));
      assertEquals("0s", DutchAuctionPricing.formatShortCountdownToward(t, t.plusSeconds(1)));
    }

    @Test
    void formatsMinutesAndSeconds_whenUnderOneHour() {
      LocalDateTime now = LocalDateTime.of(2026, 8, 1, 12, 0);
      LocalDateTime target = now.plusMinutes(25).plusSeconds(31);
      assertEquals("25m 31s", DutchAuctionPricing.formatShortCountdownToward(target, now));
    }

    @Test
    void formatsHoursAndMinutes_whenAtLeastOneHour() {
      LocalDateTime now = LocalDateTime.of(2026, 8, 1, 12, 0);
      LocalDateTime target = now.plusHours(3).plusMinutes(49);
      assertEquals("3h 49m", DutchAuctionPricing.formatShortCountdownToward(target, now));
    }

    @Test
    void formatsDays_whenAtLeastOneDay() {
      LocalDateTime now = LocalDateTime.of(2026, 8, 1, 12, 0);
      LocalDateTime target = now.plusDays(2).plusHours(14);
      assertEquals("2d 14h", DutchAuctionPricing.formatShortCountdownToward(target, now));
    }
  }

  @Nested
  @DisplayName("roundMoney / bidMatchesListedPrice")
  class MoneyRounding {

    @Test
    void roundMoney_twoDecimals() {
      assertEquals(157.13, DutchAuctionPricing.roundMoney(157.129), 1e-9);
      assertEquals("157.13$", DutchAuctionPricing.formatListedPrice(157.129));
    }

    @Test
    void bidMatchesListedPrice_acceptsRoundedBid() {
      assertTrue(DutchAuctionPricing.bidMatchesListedPrice(157.13, 157.129));
      assertFalse(DutchAuctionPricing.bidMatchesListedPrice(157.0, 157.13));
    }
  }
}
