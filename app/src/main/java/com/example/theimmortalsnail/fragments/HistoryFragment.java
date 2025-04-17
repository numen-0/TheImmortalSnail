package com.example.theimmortalsnail.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.theimmortalsnail.R;
import com.example.theimmortalsnail.models.HistoryEntry;

public class HistoryFragment extends Fragment {

    private LinearLayout historyList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        historyList = root.findViewById(R.id.historyList);

        // TODO: fetch
        addHistoryEntry(new HistoryEntry("Alberto", "01:25:00", 3200, 980));
addHistoryEntry(new com.example.theimmortalsnail.models.HistoryEntry("Manuel", "00:45:10", 1900, 450));
addHistoryEntry(new com.example.theimmortalsnail.models.HistoryEntry("Roberto", "00:23:07", 2300, 950));

        return root;
    }

    private void addHistoryEntry(HistoryEntry entry) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View card = inflater.inflate(R.layout.history_card_template, historyList, false);

        ((TextView) card.findViewById(R.id.snailName)).setText(entry.getName());
        ((TextView) card.findViewById(R.id.timeVal)).setText(entry.getTime());
        ((TextView) card.findViewById(R.id.distanceVal)).setText(entry.getDistance());
        ((TextView) card.findViewById(R.id.maxVal)).setText(entry.getMaxDistance());

        // TODO: set snail image dynamically

        historyList.addView(card);
    }
}
