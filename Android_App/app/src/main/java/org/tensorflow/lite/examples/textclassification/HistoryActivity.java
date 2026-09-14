package org.tensorflow.lite.examples.textclassification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Shows every classification saved so far, most recent first, with a
 * button to clear the log. {@link MainActivity} writes to the same
 * SharedPreferences entry right after each classification.
 */
public class HistoryActivity extends AppCompatActivity {
    static final String PREFS_NAME = "classification_history";
    static final String PREFS_KEY = "entries";

    private ListView listView;
    private TextView emptyView;
    private TextView statsView;
    private TextView confidenceFilterLabel;
    private SeekBar confidenceFilterSeekBar;
    private EditText searchInput;
    private RadioGroup sortGroup;
    private float minConfidence = 0f;
    private String searchQuery = "";
    private boolean sortByConfidence = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listView = findViewById(R.id.historyListView);
        emptyView = findViewById(R.id.historyEmptyView);
        statsView = findViewById(R.id.historyStatsView);
        confidenceFilterLabel = findViewById(R.id.confidenceFilterLabel);
        confidenceFilterSeekBar = findViewById(R.id.confidenceFilterSeekBar);
        searchInput = findViewById(R.id.historySearchInput);
        sortGroup = findViewById(R.id.historySortGroup);

        Button clearButton = findViewById(R.id.clearHistoryButton);
        clearButton.setOnClickListener((View v) -> confirmClearHistory());

        Button exportButton = findViewById(R.id.exportAllButton);
        exportButton.setOnClickListener((View v) -> exportVisibleEntries());

        confidenceFilterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minConfidence = progress / 100f;
                confidenceFilterLabel.setText(String.format(Locale.US, "Minimum confidence: %d%%", progress));
                refresh();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No-op: only the final progress value matters here.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No-op: onProgressChanged already applied the filter live.
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op: only the text after the change matters here.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                refresh();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op: onTextChanged already applied the filter live.
            }
        });

        sortGroup.setOnCheckedChangeListener((group, checkedId) -> {
            sortByConfidence = checkedId == R.id.sortByConfidence;
            refresh();
        });

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // A classification made after this activity was created (then
        // returned to via back-stack) should still show up.
        refresh();
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Clear history")
                .setMessage("Remove every saved classification? This can't be undone.")
                .setPositiveButton("Clear", (dialog, which) -> clearHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearHistory() {
        ClassificationHistoryStore store = loadStore(this);
        store.clear();
        saveStore(this, store);
        refresh();
    }

    private void refresh() {
        ClassificationHistoryStore store = loadStore(this);
        List<HistoryEntry> visible = currentlyVisibleEntries(store);

        listView.setAdapter(new HistoryAdapter(this, new ArrayList<>(visible), this::shareEntry));
        boolean empty = visible.isEmpty();
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        emptyView.setText(store.size() == 0
                ? "No classifications yet."
                : "No classifications match the current filters.");
        statsView.setText(HistoryStats.from(visible).toSummaryText());
    }

    /** Applies the confidence filter, search query, and sort order currently set in the UI. */
    private List<HistoryEntry> currentlyVisibleEntries(ClassificationHistoryStore store) {
        List<HistoryEntry> visible = filterByMinConfidence(store.getEntries(), minConfidence);
        visible = filterByQuery(visible, searchQuery);
        return sortEntries(visible, sortByConfidence);
    }

    private void shareEntry(HistoryEntry entry) {
        String shareText = String.format(Locale.US, "%s\n\nPredicted: %s (%.1f%%)",
                entry.getText(), entry.getLabel(), entry.getConfidence() * 100f);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share classification"));
    }

    private void exportVisibleEntries() {
        List<HistoryEntry> visible = currentlyVisibleEntries(loadStore(this));
        String exportText = formatEntriesForExport(visible);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, exportText);
        startActivity(Intent.createChooser(shareIntent, "Export classification history"));
    }

    /**
     * Keeps only entries whose confidence is at least minConfidence. Pulled
     * out as its own pure function (no Android types) so the filtering
     * logic can be unit tested without an Activity or SeekBar involved.
     */
    static List<HistoryEntry> filterByMinConfidence(List<HistoryEntry> entries, float minConfidence) {
        List<HistoryEntry> filtered = new ArrayList<>();
        for (HistoryEntry entry : entries) {
            if (entry.getConfidence() >= minConfidence) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    /**
     * Keeps only entries whose text or label contains query, case
     * insensitively. A blank query keeps every entry.
     */
    static List<HistoryEntry> filterByQuery(List<HistoryEntry> entries, String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.US);
        if (needle.isEmpty()) {
            return entries;
        }
        List<HistoryEntry> filtered = new ArrayList<>();
        for (HistoryEntry entry : entries) {
            boolean textMatches = entry.getText().toLowerCase(Locale.US).contains(needle);
            boolean labelMatches = entry.getLabel().toLowerCase(Locale.US).contains(needle);
            if (textMatches || labelMatches) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    /**
     * Returns a new list ordered by descending confidence when byConfidence
     * is true, or unchanged (already newest-first from the store) otherwise.
     */
    static List<HistoryEntry> sortEntries(List<HistoryEntry> entries, boolean byConfidence) {
        if (!byConfidence) {
            return entries;
        }
        List<HistoryEntry> sorted = new ArrayList<>(entries);
        Collections.sort(sorted, new Comparator<HistoryEntry>() {
            @Override
            public int compare(HistoryEntry a, HistoryEntry b) {
                return Float.compare(b.getConfidence(), a.getConfidence());
            }
        });
        return sorted;
    }

    /**
     * Renders entries as one line each, "text -> label (confidence%)", for
     * sharing as a plain-text export. A pure function so it can be tested
     * without building a share Intent.
     */
    static String formatEntriesForExport(List<HistoryEntry> entries) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            HistoryEntry entry = entries.get(i);
            sb.append(String.format(Locale.US, "%s -> %s (%.1f%%)",
                    entry.getText(), entry.getLabel(), entry.getConfidence() * 100f));
        }
        return sb.toString();
    }

    /** Loads the saved classification history for the given context. */
    static ClassificationHistoryStore loadStore(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return ClassificationHistoryStore.fromPersistedString(prefs.getString(PREFS_KEY, ""));
    }

    /** Persists the given store for the given context. */
    static void saveStore(Context context, ClassificationHistoryStore store) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(PREFS_KEY, store.toPersistedString());
        editor.apply();
    }
}
