package com.example.theimmortalsnail.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.theimmortalsnail.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button;
        // Button button = findViewById(R.id.loginButton); // TODO: MAP
        // button.setOnClickListener(v -> openActivity(GameActivity.class));

        button = findViewById(R.id.settingsButton);
        button.setOnClickListener(v -> openActivity(ConfigActivity.class));

        button = findViewById(R.id.profileButton);
        button.setOnClickListener(v -> openActivity(UserProfileActivity.class));
    }
}
