package com.example.theimmortalsnail.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.theimmortalsnail.R;
import com.example.theimmortalsnail.helpers.DBHelper;
import com.example.theimmortalsnail.models.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    private LinearLayout achievementList;
    private LayoutInflater inflater;
    private TextView emptyMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);
        achievementList = view.findViewById(R.id.achievementList);

        this.emptyMessage = view.findViewById(R.id.noAchievementsMessage);
        this.inflater = inflater;
        generateAchievements();
        return view;
    }

    private void generateAchievements() {
        if (getContext() == null) return;
        Context context = this.getContext();
        DBHelper.getAchievements(context, new DBHelper.AchievementCallback() {
            @Override
            public void onResult(List<Achievement> achievements) {
                if (achievements.isEmpty()) {
                    emptyMessage.setVisibility(View.VISIBLE);
                    return;
                }

                for (Achievement a : achievements) {
                    View card = createAchievementCard(a, inflater);
                    achievementList.addView(card);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(context, "Failed to load achievements", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    private View createAchievementCard(Achievement a, LayoutInflater inflater) {
        CardView card = (CardView) inflater.inflate(R.layout.achievement_card_template, achievementList, false);

        ImageView icon = card.findViewById(R.id.achievementIcon);
        TextView text = card.findViewById(R.id.achievementDescription);

        icon.setImageResource(a.isDone() ? R.drawable.done : R.drawable.not_done);
        text.setText(a.getDescription(getContext()));

        return card;
    }
}
