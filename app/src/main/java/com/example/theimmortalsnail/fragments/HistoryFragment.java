package com.example.theimmortalsnail.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.theimmortalsnail.R;
import com.example.theimmortalsnail.helpers.DBHelper;
import com.example.theimmortalsnail.models.SnailRecord;

import java.util.Date;
import java.util.List;

public class HistoryFragment extends Fragment {

    private LinearLayout historyList;
    private TextView emptyMessage;
    private OnSnailSelectedListener callback;

    public interface OnSnailSelectedListener {
        void snailUpdate(SnailRecord snail);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        historyList = root.findViewById(R.id.historyList);

        this.emptyMessage = root.findViewById(R.id.noRunsMessage);

        DBHelper.getFinishedRuns(requireContext(), new DBHelper.SnailCallback() {
            @Override
            public void onResult(List<SnailRecord> records) {
                if (records.isEmpty()) {
                    emptyMessage.setVisibility(View.VISIBLE);
                    return;
                }

                for (SnailRecord record : records) {
                    addHistoryEntry(record);
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSnailSelectedListener) {
            callback = (OnSnailSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnHistoryClickListener");
        }
    }

    private void addHistoryEntry(@NonNull SnailRecord entry) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View card = inflater.inflate(R.layout.history_card_template, historyList, false);

        ((ImageView) card.findViewById(R.id.snailImage)).setImageBitmap(entry.getImg());
        ((TextView) card.findViewById(R.id.snailName)).setText(entry.getName());
        ((TextView) card.findViewById(R.id.timeVal)).setText(entry.getTime());
        ((TextView) card.findViewById(R.id.distanceVal)).setText(entry.getDistance());
        ((TextView) card.findViewById(R.id.maxVal)).setText(entry.getMaxDistance());

        card.setOnClickListener(v -> {
            if (callback != null) {
                callback.snailUpdate(entry);
            }
        });

        historyList.addView(card);
    }
}
