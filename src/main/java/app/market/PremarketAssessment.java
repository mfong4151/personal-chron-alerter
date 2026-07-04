package app.market;

/**
 * Result of evaluating the premarket index gaps against the threshold.
 *
 * @param isBreached  true when at least one tracked ticker moved past the threshold
 * @param description human-readable summary of the premarket condition
 */
public record PremarketAssessment(boolean isBreached, String description) {
}
