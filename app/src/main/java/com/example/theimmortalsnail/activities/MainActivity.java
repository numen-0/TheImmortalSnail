package com.example.theimmortalsnail.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.theimmortalsnail.R;
import com.example.theimmortalsnail.helpers.MapHelper;

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
        Button mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(v -> MapHelper.checkLocationAndOpenMap(this));

        button = findViewById(R.id.settingsButton);
        button.setOnClickListener(v -> openActivity(ConfigActivity.class));

        button = findViewById(R.id.profileButton);
        button.setOnClickListener(v -> openActivity(UserProfileActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try again
                MapHelper.checkLocationAndOpenMap(this);
            } else {
                Toast.makeText(this, R.string.msg_location_perm, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
