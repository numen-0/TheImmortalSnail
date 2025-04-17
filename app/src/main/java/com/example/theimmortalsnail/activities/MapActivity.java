package com.example.theimmortalsnail.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.theimmortalsnail.R;
import com.example.theimmortalsnail.models.HistoryEntry;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;

public class MapActivity extends BaseActivity {
    private MapView mapView;
    private TextView distanceText;
    private GeoPoint playerLocation;
    private GeoPoint snailLocation;
    private Polyline pathLine;
    private Marker snailMarker;

    // private static final double SNAIL_SPEED_METERS = 0.013; // 0.013 meters/second
    private static final double SNAIL_SPEED_METERS = 13.14;
    private static final long UPDATE_INTERVAL_MS = 3000; // 3 seconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(this));

        // Back button
        Button button = findViewById(R.id.backButton);
        button.setOnClickListener(v -> closeActivity());

        // Focus button
        Button centerButton = findViewById(R.id.focusButton);
        centerButton.setOnClickListener(v -> focusOnPlayer());

        // Distance Text
        this.distanceText = findViewById(R.id.distanceTextView);

        // Map
        this.mapView = findViewById(R.id.map);
        this.playerLocation = new GeoPoint(40.4168, -3.7038); // Example: Madrid
        this.snailLocation = new GeoPoint(40.4188, -3.7020);  // Nearby

        // Load map and plot example data
        this.loadMap(playerLocation);

        this.addMarker(this.playerLocation, "You", R.drawable.location_pin);
        this.snailMarker = this.addMarker(this.snailLocation, "Snail", R.drawable.snail);
        this.drawLine();

        // Example of calling update every 3 seconds (simulate movement)
        this.mapView.postDelayed(this::updateSnailPosition, 3000);
    }
    private void loadMap(GeoPoint center) {
        this.mapView.setMultiTouchControls(true);
        this.mapView.getController().setZoom(18.0);
        this.mapView.getController().setCenter(center);
    }

    private Marker addMarker(GeoPoint point, String title, int drawableResId) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);

        Drawable d = getDrawable(drawableResId);
        if (d != null) {
            int size = 40;
            Bitmap originalBitmap = ((BitmapDrawable) d).getBitmap();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, size, size, false);
            Drawable scaledDrawable = new BitmapDrawable(getResources(), scaledBitmap);
            marker.setIcon(scaledDrawable);
        }

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        mapView.getOverlays().add( marker);

        return marker;
    }

    private void drawLine() {
        if (pathLine != null) mapView.getOverlays().remove(pathLine);

        this.pathLine = new Polyline();
        this.pathLine.setWidth(4f);
        this.pathLine.setPoints(Arrays.asList(this.playerLocation, this.snailLocation));
        mapView.getOverlays().add(0, this.pathLine);

        double distance = this.playerLocation.distanceToAsDouble(this.snailLocation);
        this.distanceText.setText(HistoryEntry.roundDistance(distance));
    }

    private void updateSnailPosition() {
        double distance = snailLocation.distanceToAsDouble(playerLocation);

        // Stop if we're already very close
        if (distance < SNAIL_SPEED_METERS * (UPDATE_INTERVAL_MS / 1000.0)) {
            snailLocation.setCoords(playerLocation.getLatitude(), playerLocation.getLongitude());
        } else {
            // Direction vector (unit vector) from snail to player
            double bearing = snailLocation.bearingTo(playerLocation);

            // Compute new position based on bearing and fixed step
            double stepMeters = SNAIL_SPEED_METERS * (UPDATE_INTERVAL_MS / 1000.0);
            GeoPoint newPosition = computeOffset(snailLocation, stepMeters, bearing);

            snailLocation.setCoords(newPosition.getLatitude(), newPosition.getLongitude());
        }

        // Update marker and line
        snailMarker.setPosition(snailLocation);
        drawLine();
        mapView.invalidate();

        // Schedule next update
        mapView.postDelayed(this::updateSnailPosition, UPDATE_INTERVAL_MS);
    }

    // Move from a start point in a given direction (bearing) for a certain distance in meters
    private GeoPoint computeOffset(GeoPoint start, double distanceMeters, double bearingDegrees) {
        double R = 6371000.0; // Earth radius in meters
        double bearing = Math.toRadians(bearingDegrees);
        double lat1 = Math.toRadians(start.getLatitude());
        double lon1 = Math.toRadians(start.getLongitude());

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distanceMeters / R)
                + Math.cos(lat1) * Math.sin(distanceMeters / R) * Math.cos(bearing));

        double lon2 = lon1 + Math.atan2(
                Math.sin(bearing) * Math.sin(distanceMeters / R) * Math.cos(lat1),
                Math.cos(distanceMeters / R) - Math.sin(lat1) * Math.sin(lat2));

        return new GeoPoint(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }

    private void focusOnPlayer() {
        if (this.playerLocation != null) {
            this.mapView.getController().setCenter(this.playerLocation);
        }
    }
}