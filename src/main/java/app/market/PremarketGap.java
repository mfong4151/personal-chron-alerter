package app.market;

/**
 * Premarket gap for a single ticker, computed from the market-data snapshot.
 *
 * @param ticker    the symbol (e.g. QQQ)
 * @param prevClose previous session close
 * @param lastPrice latest (incl. premarket) price
 * @param gapPct    percent change vs previous close; null when no premarket data
 */
public record PremarketGap(String ticker, double prevClose, double lastPrice, Double gapPct) {

  /** True when we have a usable premarket price to compare against. */
  public boolean hasData() {
    return gapPct != null;
  }

  /** True when the absolute move meets/exceeds the threshold (either direction). */
  public boolean breachesThreshold(double thresholdPct) {
    return gapPct != null && Math.abs(gapPct) >= thresholdPct;
  }
}
