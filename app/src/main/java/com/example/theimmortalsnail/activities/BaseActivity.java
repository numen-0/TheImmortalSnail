package com.example.theimmortalsnail.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyAppLocale();
    }

    @Override
    public void onResume() {
        super.onResume();
        applyAppLocale();
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyAppLocale();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(newBase);
        String language = prefs.getString("language", "en");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        switch (prefs.getString("theme", "system")) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        Context newContext = newBase.createConfigurationContext(config);
        super.attachBaseContext(newContext);
    }

    public void restartAppWithSettings() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        Intent settingsIntent = new Intent(this, ConfigActivity.class);

        // Clear the current stack and start with MainActivity
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivities(new Intent[]{mainIntent, settingsIntent});
        finish();
    }

    public void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    public void closeActivity() {
        this.finish();
    }

    private void applyAppLocale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String langCode = prefs.getString("language", "en");

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        if (!locale.equals(config.getLocales().get(0))) {
            config.setLocale(locale);
            config.setLayoutDirection(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }
}
