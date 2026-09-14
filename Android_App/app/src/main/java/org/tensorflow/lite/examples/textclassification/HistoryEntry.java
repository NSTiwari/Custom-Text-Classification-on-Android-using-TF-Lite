package org.tensorflow.lite.examples.textclassification;

import java.util.ArrayList;
import java.util.List;

/**
 * One saved classification result: the input text, the top predicted
 * label, its confidence, and when it happened.
 *
 * Serializes to a single line so a whole history can be stored as plain
 * text (in SharedPreferences) without a JSON dependency, with one line per
 * entry. Backslash, the field delimiter, and any newline in the text or
 * label are escaped, since java.util.Base64 needs API 26+ and this app's
 * minSdk is 21 — an unescaped newline would otherwise be indistinguishable
 * from the separator between two entries.
 */
public class HistoryEntry {
    private static final char DELIMITER = '|';
    private static final char ESCAPE = '\\';

    private final String text;
    private final String label;
    private final float confidence;
    private final long timestampMillis;

    public HistoryEntry(String text, String label, float confidence, long timestampMillis) {
        this.text = text;
        this.label = label;
        this.confidence = confidence;
        this.timestampMillis = timestampMillis;
    }

    public String getText() {
        return text;
    }

    public String getLabel() {
        return label;
    }

    public float getConfidence() {
        return confidence;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public String serialize() {
        return escape(text) + DELIMITER + escape(label) + DELIMITER + confidence + DELIMITER + timestampMillis;
    }

    /**
     * Returns null if the line is malformed rather than throwing, since one
     * corrupted line shouldn't take down the whole saved history.
     */
    public static HistoryEntry parse(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        String[] parts = splitEscaped(line);
        if (parts.length != 4) {
            return null;
        }
        try {
            String text = unescape(parts[0]);
            String label = unescape(parts[1]);
            float confidence = Float.parseFloat(parts[2]);
            long timestampMillis = Long.parseLong(parts[3]);
            return new HistoryEntry(text, label, confidence, timestampMillis);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String escape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ESCAPE) {
                sb.append(ESCAPE).append(ESCAPE);
            } else if (c == DELIMITER) {
                sb.append(ESCAPE).append(DELIMITER);
            } else if (c == '\n') {
                sb.append(ESCAPE).append('n');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String unescape(String value) {
        StringBuilder sb = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaping) {
                sb.append(c == 'n' ? '\n' : c);
                escaping = false;
            } else if (c == ESCAPE) {
                escaping = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Splits on an unescaped DELIMITER only. Escape sequences are left
     * intact in each returned piece — unescape() resolves them afterwards,
     * so a character is never decoded twice.
     */
    private static String[] splitEscaped(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaping) {
                current.append(c);
                escaping = false;
            } else if (c == ESCAPE) {
                current.append(c);
                escaping = true;
            } else if (c == DELIMITER) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }
}
