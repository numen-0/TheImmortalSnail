package com.example.theimmortalsnail.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.theimmortalsnail.R;
import com.example.theimmortalsnail.fragments.AchievementsFragment;
import com.example.theimmortalsnail.fragments.HistoryFragment;

public class UserProfileActivity extends BaseActivity {
    private boolean isHistoryShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button switchButton = findViewById(R.id.switchButton);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.profileFragmentContainer, new AchievementsFragment())
                .commit();
        this.isHistoryShowing = false;

        switchButton.setOnClickListener(v -> {
            Fragment fragment;
            if (isHistoryShowing) {
                fragment = new AchievementsFragment();
                switchButton.setText(R.string.achievements);
                isHistoryShowing = false;
            } else {
                fragment = new HistoryFragment();
                switchButton.setText(R.string.history);
                isHistoryShowing = true;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.profileFragmentContainer, fragment)
                    .commit();
        });

        Button button = findViewById(R.id.backButton);
        button.setOnClickListener(v -> closeActivity());
    }
}