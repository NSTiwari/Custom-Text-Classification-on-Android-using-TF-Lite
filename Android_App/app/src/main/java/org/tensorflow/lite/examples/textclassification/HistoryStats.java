package org.tensorflow.lite.examples.textclassification;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A small summary of a list of {@link HistoryEntry}: how many, their mean
 * confidence, and which predicted label came up most often. Pure Java so
 * it can be unit tested without an Activity.
 */
public class HistoryStats {
    private final int count;
    private final float averageConfidence;
    private final String mostCommonLabel;

    private HistoryStats(int count, float averageConfidence, String mostCommonLabel) {
        this.count = count;
        this.averageConfidence = averageConfidence;
        this.mostCommonLabel = mostCommonLabel;
    }

    public int getCount() {
        return count;
    }

    public float getAverageConfidence() {
        return averageConfidence;
    }

    /** Null when there are no entries to summarize. */
    public String getMostCommonLabel() {
        return mostCommonLabel;
    }

    public static HistoryStats from(List<HistoryEntry> entries) {
        if (entries.isEmpty()) {
            return new HistoryStats(0, 0f, null);
        }

        float confidenceSum = 0f;
        Map<String, Integer> labelCounts = new HashMap<>();
        for (HistoryEntry entry : entries) {
            confidenceSum += entry.getConfidence();
            Integer current = labelCounts.get(entry.getLabel());
            labelCounts.put(entry.getLabel(), (current == null ? 0 : current) + 1);
        }

        String mostCommonLabel = null;
        int highestCount = -1;
        for (Map.Entry<String, Integer> labelCount : labelCounts.entrySet()) {
            if (labelCount.getValue() > highestCount) {
                highestCount = labelCount.getValue();
                mostCommonLabel = labelCount.getKey();
            }
        }

        return new HistoryStats(entries.size(), confidenceSum / entries.size(), mostCommonLabel);
    }

    /** A one-line summary for display, e.g. "12 results - avg 78.3% - most common: Positive". */
    public String toSummaryText() {
        if (count == 0) {
            return "No results yet.";
        }
        return String.format(Locale.US, "%d result%s - avg %.1f%% - most common: %s",
                count, count == 1 ? "" : "s", averageConfidence * 100f, mostCommonLabel);
    }
}
