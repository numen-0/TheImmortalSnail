package com.example.theimmortalsnail.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.theimmortalsnail.activities.MapActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class MapHelper {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    public static void checkLocationAndOpenMap(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(activity, "Please enable location services", Toast.LENGTH_LONG).show();
            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } else {
            Intent intent = new Intent(activity, MapActivity.class);
            activity.startActivity(intent);
        }
    }

    @SuppressLint("MissingPermission")
    public static FusedLocationProviderClient genProvider(Activity activity, LocationCallback callback) {
        FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build();

        locationProviderClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper());

        return locationProviderClient;
    }
}
