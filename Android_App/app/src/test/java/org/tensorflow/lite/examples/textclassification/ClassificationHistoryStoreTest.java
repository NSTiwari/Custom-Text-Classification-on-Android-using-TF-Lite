package org.tensorflow.lite.examples.textclassification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class ClassificationHistoryStoreTest {

    private static HistoryEntry entry(String text) {
        return new HistoryEntry(text, "Label", 0.9f, 1000L);
    }

    @Test
    public void add_putsNewestEntryFirst() {
        ClassificationHistoryStore store = new ClassificationHistoryStore();

        store.add(entry("first"));
        store.add(entry("second"));

        List<HistoryEntry> entries = store.getEntries();
        assertEquals(2, entries.size());
        assertEquals("second", entries.get(0).getText());
        assertEquals("first", entries.get(1).getText());
    }

    @Test
    public void add_trimsToMaxEntries() {
        ClassificationHistoryStore store = new ClassificationHistoryStore();

        for (int i = 0; i < ClassificationHistoryStore.MAX_ENTRIES + 10; i++) {
            store.add(entry("entry-" + i));
        }

        assertEquals(ClassificationHistoryStore.MAX_ENTRIES, store.size());
        assertEquals("entry-" + (ClassificationHistoryStore.MAX_ENTRIES + 9), store.getEntries().get(0).getText());
    }

    @Test
    public void clear_removesEveryEntry() {
        ClassificationHistoryStore store = new ClassificationHistoryStore();
        store.add(entry("first"));

        store.clear();

        assertEquals(0, store.size());
    }

    @Test
    public void toPersistedString_thenFromPersistedString_roundTrips() {
        ClassificationHistoryStore store = new ClassificationHistoryStore();
        store.add(entry("first"));
        store.add(entry("second"));

        ClassificationHistoryStore restored = ClassificationHistoryStore.fromPersistedString(store.toPersistedString());

        List<HistoryEntry> entries = restored.getEntries();
        assertEquals(2, entries.size());
        assertEquals("second", entries.get(0).getText());
        assertEquals("first", entries.get(1).getText());
    }

    @Test
    public void fromPersistedString_withEmptyString_returnsEmptyStore() {
        ClassificationHistoryStore store = ClassificationHistoryStore.fromPersistedString("");

        assertEquals(0, store.size());
    }

    @Test
    public void fromPersistedString_skipsCorruptedLinesWithoutFailing() {
        String persisted = entry("good").serialize() + "\n" + "not|a|valid|entry|too|many|fields";

        ClassificationHistoryStore store = ClassificationHistoryStore.fromPersistedString(persisted);

        assertEquals(1, store.size());
        assertTrue(store.getEntries().get(0).getText().equals("good"));
    }
}
