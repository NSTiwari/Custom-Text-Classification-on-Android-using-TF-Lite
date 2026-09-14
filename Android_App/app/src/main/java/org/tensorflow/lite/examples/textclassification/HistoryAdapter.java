package org.tensorflow.lite.examples.textclassification;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Renders one {@link HistoryEntry} per row: the classified text, the
 * predicted label with its confidence, a relative timestamp, and a button
 * to share that single result.
 */
public class HistoryAdapter extends ArrayAdapter<HistoryEntry> {

    /** Notified when the row's Share button is tapped, for the given entry. */
    public interface OnShareClickListener {
        void onShareClick(HistoryEntry entry);
    }

    private static class ViewHolder {
        TextView textView;
        TextView labelView;
        TextView timeView;
        Button shareButton;
    }

    private final OnShareClickListener shareClickListener;

    public HistoryAdapter(Context context, List<HistoryEntry> entries, OnShareClickListener shareClickListener) {
        super(context, 0, entries);
        this.shareClickListener = shareClickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_history, parent, false);
            holder = new ViewHolder();
            holder.textView = view.findViewById(R.id.historyText);
            holder.labelView = view.findViewById(R.id.historyLabel);
            holder.timeView = view.findViewById(R.id.historyTime);
            holder.shareButton = view.findViewById(R.id.historyShareButton);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        HistoryEntry entry = getItem(position);
        if (entry != null) {
            holder.textView.setText(entry.getText());
            holder.labelView.setText(formatLabel(entry));
            holder.timeView.setText(DateUtils.getRelativeTimeSpanString(entry.getTimestampMillis()));
            holder.shareButton.setOnClickListener((View v) -> {
                if (shareClickListener != null) {
                    shareClickListener.onShareClick(entry);
                }
            });
        }

        return view;
    }

    private static String formatLabel(HistoryEntry entry) {
        return String.format(Locale.US, "%s (%.1f%%)", entry.getLabel(), entry.getConfidence() * 100f);
    }
}
