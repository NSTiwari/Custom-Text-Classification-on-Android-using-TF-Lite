package org.tensorflow.lite.examples.textclassification;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Covers {@link HistoryActivity#filterByMinConfidence}, the pure part of
 * the confidence-filter feature, without touching the Activity itself.
 */
public class HistoryActivityFilterTest {

    private static HistoryEntry entryWithConfidence(float confidence) {
        return new HistoryEntry("text", "label", confidence, 0L);
    }

    @Test
    public void filterByMinConfidence_keepsEntriesAtOrAboveThreshold() {
        List<HistoryEntry> entries = Arrays.asList(
                entryWithConfidence(0.9f),
                entryWithConfidence(0.5f),
                entryWithConfidence(0.2f)
        );

        List<HistoryEntry> filtered = HistoryActivity.filterByMinConfidence(entries, 0.5f);

        assertEquals(2, filtered.size());
    }

    @Test
    public void filterByMinConfidence_zeroThresholdKeepsEverything() {
        List<HistoryEntry> entries = Arrays.asList(entryWithConfidence(0.01f), entryWithConfidence(0f));

        List<HistoryEntry> filtered = HistoryActivity.filterByMinConfidence(entries, 0f);

        assertEquals(2, filtered.size());
    }

    @Test
    public void filterByMinConfidence_thresholdAboveEveryEntryReturnsEmpty() {
        List<HistoryEntry> entries = Arrays.asList(entryWithConfidence(0.3f), entryWithConfidence(0.4f));

        List<HistoryEntry> filtered = HistoryActivity.filterByMinConfidence(entries, 0.5f);

        assertEquals(0, filtered.size());
    }

    @Test
    public void filterByQuery_matchesTextCaseInsensitively() {
        List<HistoryEntry> entries = Arrays.asList(
                new HistoryEntry("Great Product", "Positive", 0.9f, 0L),
                new HistoryEntry("Terrible service", "Negative", 0.9f, 0L)
        );

        List<HistoryEntry> filtered = HistoryActivity.filterByQuery(entries, "great");

        assertEquals(1, filtered.size());
        assertEquals("Great Product", filtered.get(0).getText());
    }

    @Test
    public void filterByQuery_matchesLabelToo() {
        List<HistoryEntry> entries = Arrays.asList(
                new HistoryEntry("Some text", "Positive", 0.9f, 0L),
                new HistoryEntry("Other text", "Negative", 0.9f, 0L)
        );

        List<HistoryEntry> filtered = HistoryActivity.filterByQuery(entries, "negative");

        assertEquals(1, filtered.size());
        assertEquals("Other text", filtered.get(0).getText());
    }

    @Test
    public void filterByQuery_blankQueryKeepsEverything() {
        List<HistoryEntry> entries = Arrays.asList(entryWithConfidence(0.1f), entryWithConfidence(0.2f));

        assertEquals(2, HistoryActivity.filterByQuery(entries, "  ").size());
        assertEquals(2, HistoryActivity.filterByQuery(entries, null).size());
    }

    @Test
    public void sortEntries_byConfidenceOrdersDescending() {
        HistoryEntry low = entryWithConfidence(0.2f);
        HistoryEntry high = entryWithConfidence(0.9f);
        HistoryEntry mid = entryWithConfidence(0.5f);
        List<HistoryEntry> entries = Arrays.asList(low, high, mid);

        List<HistoryEntry> sorted = HistoryActivity.sortEntries(entries, true);

        assertEquals(high, sorted.get(0));
        assertEquals(mid, sorted.get(1));
        assertEquals(low, sorted.get(2));
    }

    @Test
    public void sortEntries_notByConfidenceReturnsOriginalOrder() {
        HistoryEntry first = entryWithConfidence(0.2f);
        HistoryEntry second = entryWithConfidence(0.9f);
        List<HistoryEntry> entries = Arrays.asList(first, second);

        List<HistoryEntry> sorted = HistoryActivity.sortEntries(entries, false);

        assertEquals(first, sorted.get(0));
        assertEquals(second, sorted.get(1));
    }

    @Test
    public void formatEntriesForExport_joinsOneLinePerEntry() {
        List<HistoryEntry> entries = Arrays.asList(
                new HistoryEntry("Great product", "Positive", 0.876f, 0L),
                new HistoryEntry("Bad service", "Negative", 0.5f, 0L)
        );

        String exported = HistoryActivity.formatEntriesForExport(entries);

        assertEquals("Great product -> Positive (87.6%)\nBad service -> Negative (50.0%)", exported);
    }

    @Test
    public void formatEntriesForExport_emptyListReturnsEmptyString() {
        assertEquals("", HistoryActivity.formatEntriesForExport(Arrays.asList()));
    }
}
