package org.tensorflow.lite.examples.textclassification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HistoryStatsTest {

    @Test
    public void from_emptyList_returnsZeroedStats() {
        HistoryStats stats = HistoryStats.from(Collections.emptyList());

        assertEquals(0, stats.getCount());
        assertEquals(0f, stats.getAverageConfidence(), 0.0001f);
        assertNull(stats.getMostCommonLabel());
        assertEquals("No results yet.", stats.toSummaryText());
    }

    @Test
    public void from_computesAverageConfidence() {
        List<HistoryEntry> entries = Arrays.asList(
                new HistoryEntry("a", "Positive", 1.0f, 0L),
                new HistoryEntry("b", "Positive", 0.5f, 0L)
        );

        HistoryStats stats = HistoryStats.from(entries);

        assertEquals(2, stats.getCount());
        assertEquals(0.75f, stats.getAverageConfidence(), 0.0001f);
    }

    @Test
    public void from_picksTheMostFrequentLabel() {
        List<HistoryEntry> entries = Arrays.asList(
                new HistoryEntry("a", "Positive", 0.9f, 0L),
                new HistoryEntry("b", "Negative", 0.8f, 0L),
                new HistoryEntry("c", "Negative", 0.7f, 0L)
        );

        HistoryStats stats = HistoryStats.from(entries);

        assertEquals("Negative", stats.getMostCommonLabel());
    }

    @Test
    public void toSummaryText_singularWordingForOneResult() {
        HistoryStats stats = HistoryStats.from(Arrays.asList(new HistoryEntry("a", "Positive", 0.876f, 0L)));

        assertEquals("1 result - avg 87.6% - most common: Positive", stats.toSummaryText());
    }
}
