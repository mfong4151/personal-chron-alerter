package app.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;

import app.clients.MassiveApiClient;

/**
 * Queries the market-data snapshot for the index ETFs and evaluates each
 * ticker's premarket gap against a configurable threshold. Produces a factual
 * condition summary to feed into the research prompt, replacing the previous
 * approach of asking OpenAI to determine the gap itself.
 */
public final class PremarketGapService {

  private static final List<String> INDEX_TICKERS = List.of("QQQ", "SPY");
  private static final String SNAPSHOT_PATH =
      "/v2/snapshot/locale/us/markets/stocks/tickers/";

  private final MassiveApiClient client;
  private final double thresholdPct;

  public PremarketGapService(MassiveApiClient client, double thresholdPct) {
    this.client = client;
    this.thresholdPct = thresholdPct;
  }

  /** Fetch and compute the premarket gap for a single ticker. */
  public PremarketGap fetchGap(String ticker) {
    JsonNode root = client.get(SNAPSHOT_PATH + ticker);
    JsonNode t = root.path("ticker");

    double prevClose = t.path("prevDay").path("c").asDouble(0.0);
    double lastPrice = t.path("min").path("c").asDouble(0.0);

    Double gapPct = null;
    if (prevClose > 0.0 && lastPrice > 0.0) {
      gapPct = (lastPrice - prevClose) / prevClose * 100.0;
    }
    return new PremarketGap(ticker, prevClose, lastPrice, gapPct);
  }

  /** Fetch gaps for all tracked index ETFs (QQQ, SPY). */
  public List<PremarketGap> fetchIndexGaps() {
    List<PremarketGap> gaps = new ArrayList<>();
    for (String ticker : INDEX_TICKERS) {
      gaps.add(fetchGap(ticker));
    }
    return gaps;
  }

  /**
   * Builds a human-readable, factual statement of the current premarket
   * condition for QQQ and SPY relative to the threshold. This is injected into
   * the research prompt so the LLM only has to explain causes, not detect the move.
   */
  public String describeCondition() {
    List<PremarketGap> gaps = fetchIndexGaps();

    boolean anyData = gaps.stream().anyMatch(PremarketGap::hasData);
    if (!anyData) {
      return String.format(Locale.US,
          "No premarket data is available for QQQ or SPY yet (market closed or "
              + "before premarket). Treat this as no move past the %.2f%% threshold.",
          thresholdPct);
    }

    boolean anyBreach = gaps.stream().anyMatch(g -> g.breachesThreshold(thresholdPct));

    StringBuilder sb = new StringBuilder();
    sb.append(String.format(Locale.US, "Premarket move (threshold %.2f%%): ", thresholdPct));

    List<String> parts = new ArrayList<>();
    for (PremarketGap g : gaps) {
      if (g.hasData()) {
        parts.add(String.format(Locale.US, "%s is %+.2f%% (last %.2f vs prev close %.2f)",
            g.ticker(), g.gapPct(), g.lastPrice(), g.prevClose()));
      } else {
        parts.add(String.format(Locale.US, "%s has no premarket data", g.ticker()));
      }
    }
    sb.append(String.join("; ", parts)).append(". ");

    if (anyBreach) {
      sb.append(String.format(Locale.US,
          "This is ABOVE the %.2f%% threshold — a notable premarket gap.", thresholdPct));
    } else {
      sb.append(String.format(Locale.US,
          "This is BELOW the %.2f%% threshold — no notable premarket gap.", thresholdPct));
    }
    return sb.toString();
  }
}
