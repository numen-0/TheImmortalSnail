package com.example.theimmortalsnail.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceFragmentCompat;

import com.example.theimmortalsnail.activities.BaseActivity;
import com.example.theimmortalsnail.NotificationHelper;
import com.example.theimmortalsnail.R;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        System.out.println("Preference changed: " + key);

        assert key != null;
        switch (key) {
            case "language":
                String langCode = sharedPreferences.getString(key, "en");
                setAppLocale(langCode);
                break;
            case "theme":
                String themePref = sharedPreferences.getString(key, "system");
                switch (themePref) {
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
                break;
            case "enable_notifications":
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED) {
                    boolean notificationsEnabled = sharedPreferences.getBoolean(key, true);

                    if (notificationsEnabled) {
                        NotificationHelper.scheduleDailyNotification(requireContext());
                    } else {
                        NotificationHelper.cancelDailyNotification(requireContext());
                    }
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        android.Manifest.permission.POST_NOTIFICATIONS)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            NotificationHelper.NOTIFICATION_ID);
                }
                break;
        }
    }


    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        // Update the context
        requireActivity().getBaseContext().getResources().updateConfiguration(
                config,
                requireActivity().getBaseContext().getResources().getDisplayMetrics()
        );

        // Restart activity to apply changes
        ((BaseActivity) requireActivity()).restartAppWithSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}