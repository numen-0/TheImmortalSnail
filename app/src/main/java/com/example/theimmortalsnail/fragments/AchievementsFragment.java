package com.example.theimmortalsnail.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.theimmortalsnail.R;
import com.example.theimmortalsnail.models.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    private LinearLayout achievementList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);
        achievementList = view.findViewById(R.id.achievementList);

        List<Achievement> data = generateAchievements();
        for (Achievement a : data) {
            View card = createAchievementCard(a, inflater);
            achievementList.addView(card);
        }
        return view;
    }

    private List<Achievement> generateAchievements() {
        List<Achievement> list = new ArrayList<>();
        list.add(new Achievement("Walked 1 km without getting caught!", true));
        list.add(new Achievement("Evade the snail for 10 hours straight", false));
        list.add(new Achievement("Stayed alive for a full week", false));
        list.add(new Achievement("Ran in circles for 3km", true));
        list.add(new Achievement("._.", true));
        list.add(new Achievement("fuck", true));
        list.add(new Achievement("yeyeye", true));
        list.add(new Achievement("hiiiiiiiiiiiiiii iiiiiiiiiiiiiii iiiiiiiii i iiiiiiiiiiiiiiiiiiii iiiiii iiiiiiii iiiiiiiiiiiii", true));
        list.add(new Achievement("._.", true));
        list.add(new Achievement("fuck", true));
        list.add(new Achievement("yeyeye", true));
        return list;
    }

    private View createAchievementCard(Achievement a, LayoutInflater inflater) {
        CardView card = (CardView) inflater.inflate(R.layout.achievement_card_template, achievementList, false);

        ImageView icon = card.findViewById(R.id.achievementIcon);
        TextView text = card.findViewById(R.id.achievementDescription);

        icon.setImageResource(a.done ? R.drawable.done : R.drawable.not_done);
        text.setText(a.description);

        return card;
    }
}
