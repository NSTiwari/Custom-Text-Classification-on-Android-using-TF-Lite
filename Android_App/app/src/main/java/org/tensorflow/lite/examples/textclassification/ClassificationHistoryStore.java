package org.tensorflow.lite.examples.textclassification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An in-memory, newest-first list of past classifications, capped so the
 * persisted string doesn't grow forever. Pure Java (no Android APIs) so it
 * can be unit tested directly; {@link HistoryActivity} owns reading and
 * writing it to SharedPreferences.
 */
public class ClassificationHistoryStore {
    static final int MAX_ENTRIES = 200;
    private static final char LINE_DELIMITER = '\n';

    private final List<HistoryEntry> entries;

    public ClassificationHistoryStore() {
        this.entries = new ArrayList<>();
    }

    private ClassificationHistoryStore(List<HistoryEntry> entries) {
        this.entries = entries;
    }

    public void add(HistoryEntry entry) {
        entries.add(0, entry);
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
    }

    public List<HistoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

    public String toPersistedString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                sb.append(LINE_DELIMITER);
            }
            sb.append(entries.get(i).serialize());
        }
        return sb.toString();
    }

    public static ClassificationHistoryStore fromPersistedString(String persisted) {
        List<HistoryEntry> parsed = new ArrayList<>();
        if (persisted != null && !persisted.isEmpty()) {
            for (String line : persisted.split(String.valueOf(LINE_DELIMITER))) {
                HistoryEntry entry = HistoryEntry.parse(line);
                if (entry != null) {
                    parsed.add(entry);
                }
            }
        }
        return new ClassificationHistoryStore(parsed);
    }
}
