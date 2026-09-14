package org.tensorflow.lite.examples.textclassification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class HistoryEntryTest {

    @Test
    public void serializeThenParse_roundTripsPlainValues() {
        HistoryEntry original = new HistoryEntry("Great product", "Positive", 0.87f, 1700000000000L);

        HistoryEntry parsed = HistoryEntry.parse(original.serialize());

        assertEquals("Great product", parsed.getText());
        assertEquals("Positive", parsed.getLabel());
        assertEquals(0.87f, parsed.getConfidence(), 0.0001f);
        assertEquals(1700000000000L, parsed.getTimestampMillis());
    }

    @Test
    public void serializeThenParse_roundTripsTextContainingTheDelimiter() {
        HistoryEntry original = new HistoryEntry("a | pipe in the text", "Neutral", 0.5f, 42L);

        HistoryEntry parsed = HistoryEntry.parse(original.serialize());

        assertEquals("a | pipe in the text", parsed.getText());
        assertEquals("Neutral", parsed.getLabel());
    }

    @Test
    public void serializeThenParse_roundTripsTextContainingABackslash() {
        HistoryEntry original = new HistoryEntry("path\\to\\file", "Label", 0.5f, 42L);

        HistoryEntry parsed = HistoryEntry.parse(original.serialize());

        assertEquals("path\\to\\file", parsed.getText());
    }

    @Test
    public void serializeThenParse_roundTripsTextContainingANewline() {
        HistoryEntry original = new HistoryEntry("line one\nline two", "Label", 0.5f, 42L);

        HistoryEntry parsed = HistoryEntry.parse(original.serialize());

        assertEquals("line one\nline two", parsed.getText());
    }

    @Test
    public void parse_returnsNullForBlankLine() {
        assertNull(HistoryEntry.parse(""));
    }

    @Test
    public void parse_returnsNullForNullLine() {
        assertNull(HistoryEntry.parse(null));
    }

    @Test
    public void parse_returnsNullForTooFewFields() {
        assertNull(HistoryEntry.parse("only|three|fields"));
    }

    @Test
    public void parse_returnsNullForNonNumericConfidence() {
        assertNull(HistoryEntry.parse("text|label|not-a-number|42"));
    }
}
