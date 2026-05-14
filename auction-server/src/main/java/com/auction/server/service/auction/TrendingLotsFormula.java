package com.auction.server.service.auction;

/**
 * Trending score for live auctions from aggregated bid activity.
 *
 * <p><b>Decay (interaction fades over time):</b> each bid contributes weight {@code exp(-h/τ)}
 * where {@code h} is age in hours and τ = {@link #TAU_DECAY_HALF_LIFE_HOURS} (effective decay scale;
 * bids older than τ hours contribute less).
 *
 * <p><b>Long-window mass {@code W_long}:</b> sum of decay weights over all bids in the last {@link
 * #LONG_WINDOW_HOURS} hours (computed in SQL and passed here).
 *
 * <p><b>Short-window mass {@code W_short}:</b> same decay, but only bids in the last {@link
 * #SHORT_WINDOW_MINUTES} minutes → captures bursts (“10 bids in 5 min” beats evenly spread bids).
 *
 * <p><b>Burst factor {@code V}:</b> {@code ln(1 + W_short / (ε + max(0, W_long - W_short)))}.
 *
 * <p><b>Diversity {@code D}:</b> {@code min(U, U_cap) / U_cap} where U is distinct bidder count in the
 * long window (capped so one whale cannot dominate purely by account count above cap).
 *
 * <p><b>Final score:</b> {@code ln(1 + W_long) * (1 + α*D) * (1 + β*V)}
 */
public final class TrendingLotsFormula {

  /** Hours: only bids newer than this are included in aggregates (matches SQL WHERE). */
  public static final int LONG_WINDOW_HOURS = 72;

  /** Minutes: “burst” numerator — bids in this fresh window weigh into {@code W_short}. */
  public static final int SHORT_WINDOW_MINUTES = 15;

  /**
   * Decay τ in hours: weight is exp(-ageHours/τ). Higher τ ⇒ older bids still matter more
   * (interaction decays slower).
   */
  public static final double TAU_DECAY_HOURS = 36.0;

  /** Normalize distinct bidders — values above cap are treated as cap. */
  public static final int DISTINCT_BIDDERS_CAP = 20;

  /** Weight for diversity multiplier {@code (1 + α·D)}. */
  public static final double ALPHA_DIVERSITY = 0.45;

  /** Weight for burst multiplier {@code (1 + β·V)}. */
  public static final double BETA_BURST = 1.35;

  public static final double EPS = 1e-9;

  private TrendingLotsFormula() {}

  /**
   * @param wLong sum of decay weights in the long bid window
   * @param wShort sum of decay weights for bids in the short (fresh) window only
   * @param distinctBidders distinct {@code userid} count in the long window
   */
  public static double computeTrendScore(double wLong, double wShort, int distinctBidders) {
    double wL = Math.max(0.0, wLong);
    double wS = Math.max(0.0, Math.min(wSOrZero(wShort), wL + EPS));
    double tail = Math.max(0.0, wL - wS);
    double v = Math.log1p(wS / (EPS + tail));
    double d =
        Math.min(Math.max(0, distinctBidders), DISTINCT_BIDDERS_CAP)
            / (double) DISTINCT_BIDDERS_CAP;
    return Math.log1p(wL) * (1.0 + ALPHA_DIVERSITY * d) * (1.0 + BETA_BURST * v);
  }

  private static double wSOrZero(double wShort) {
    return Double.isFinite(wShort) ? wShort : 0.0;
  }
}
