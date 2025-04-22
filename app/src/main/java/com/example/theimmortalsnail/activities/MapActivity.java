package com.example.theimmortalsnail.activities;

import static com.example.theimmortalsnail.helpers.MapHelper.snailId;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.theimmortalsnail.R;
import com.example.theimmortalsnail.helpers.DBHelper;
import com.example.theimmortalsnail.helpers.MapHelper;
import com.example.theimmortalsnail.models.SnailRecord;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;
import java.util.Random;

public class MapActivity extends BaseActivity {
    private MapView mapView;
    private TextView distanceText;
    private GeoPoint playerLocation;
    private GeoPoint snailLocation;
    private Polyline pathLine;
    private Marker snailMarker;
    private Marker playerMarker;
    private LocationCallback locationCallback;
    private boolean pause;
    private boolean wait;
    private boolean end;
    private float totalDistance;
    private float distanceFromUser;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final double SNAIL_SPEED_METERS = 0.5d;
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

        this.totalDistance = 0.0f;
        this.distanceFromUser = 0.0f;
        this.end = false;

        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(this));

        // Back button
        Button button = findViewById(R.id.backButton);
        button.setOnClickListener(v -> {
            this.endGame();
            this.exitActivity();
        });

        // Focus button
        Button centerButton = findViewById(R.id.focusButton);
        centerButton.setOnClickListener(v -> focusOnPlayer());

        // Distance Text
        this.distanceText = findViewById(R.id.distanceTextView);
 // Map
        this.mapView = findViewById(R.id.map);
        this.playerLocation = new GeoPoint(0.0f, 0.0f);
        this.snailLocation = new GeoPoint(0.0f, 0.0f);

        // HACK: One-time location to center the map :)
        final FusedLocationProviderClient[] oneTimeProvider = new FusedLocationProviderClient[1];

        LocationCallback firstLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Update and Focus
                locationCallback.onLocationResult(locationResult);
                final int radius = (new Random().nextInt() % 3500) + 1000; // [1000, 4499]
                final double deg = new Random().nextDouble() * Math.PI * 2;
                double deltaLat = radius * Math.sin(deg) / 111320.0;
                double deltaLon = radius * Math.cos(deg) / (40008000.0 / 360.0);

                // Update snail location with the calculated offset
                snailLocation.setCoords(playerLocation.getLatitude() + deltaLat,
                        playerLocation.getLongitude() + deltaLon);
                // Load map and plot
                loadMap(playerLocation);
                playerMarker = addMarker(playerLocation, "You", R.drawable.location_pin, Marker.ANCHOR_BOTTOM);
                snailMarker = addMarker(snailLocation, MapHelper.snailName, R.drawable.snail, Marker.ANCHOR_CENTER);
                drawLine();

                focusOnPlayer();
                updateMap();

                wait = false;
                if (oneTimeProvider[0] != null) { // Remove this callback
                    oneTimeProvider[0].removeLocationUpdates(this);
                }
            }
        };
        oneTimeProvider[0] = MapHelper.genProvider(this, firstLocationCallback);

        // Location provider
        this.pause = false;
        this.wait = true;
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if ( location != null ) {
                    System.out.println("Latitude:  " + location.getLatitude());
                    System.out.println("Longitude: " + location.getLongitude());

                    playerLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                    if ( !wait && !pause ) { updateMap(); }
                } else{
                    System.out.println("Latitude:  ??");
                    System.out.println("Longitude: ??");
                }
            }
        };
        this.fusedLocationProviderClient = MapHelper.genProvider(this, locationCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        pause = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        pause = true;
        if (!isFinishing()) {
            gameOver();
        }
    }
    private void loadMap(GeoPoint center) {
        this.mapView.setMultiTouchControls(true);
        this.mapView.getController().setZoom(18.0);
        this.mapView.getController().setCenter(center);
    }

    private Marker addMarker(GeoPoint point, String title, int drawableResId, float vOffset) {
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

        marker.setAnchor(Marker.ANCHOR_CENTER, vOffset);
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
        this.distanceText.setText(SnailRecord.roundDistance(distance));
    }

    private void updateMap() {
        double distance = snailLocation.distanceToAsDouble(playerLocation);
        this.distanceFromUser = (float)distance;

        // Stop if we're already very close
        if (distance < SNAIL_SPEED_METERS * (UPDATE_INTERVAL_MS / 1000.0)) {
            snailLocation.setCoords(playerLocation.getLatitude(), playerLocation.getLongitude());

            this.distanceFromUser = 0.0f;
            gameOver();
            exitActivity();
        } else {
            // Move the snail based on the bearing and step size
            double bearing = snailLocation.bearingTo(playerLocation);
            double stepMeters = SNAIL_SPEED_METERS * (UPDATE_INTERVAL_MS / 1000.0);
            GeoPoint newPosition = computeOffset(snailLocation, stepMeters, bearing);
            this.totalDistance += (float) stepMeters;

            snailLocation.setCoords(newPosition.getLatitude(), newPosition.getLongitude());
            sendSnailUpdateToServer();
        }

        // Update marker and line
        snailMarker.setPosition(snailLocation);
        playerMarker.setPosition(playerLocation);
        drawLine();
        mapView.invalidate();
    }

    private void endGame() {
        if (this.end) { return; }
        Activity activity = this;
        sendSnailUpdateToServer();
        DBHelper.endRun(MapHelper.snailId, new DBHelper.GenericCallback() {
            @Override
            public void onSuccess(String message, Integer snailId) {
                runOnUiThread(() -> Toast.makeText(activity, "Game saved successfully", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(activity, "Error saving game", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void gameOver() {
        if (this.end) { return; }
        endGame();
        this.end = true;

        // Create an AlertDialog to show the "Game Over" message
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("You were caught by the snail! Game Over!")
                .setCancelable(false)
                .setPositiveButton("Back", (dialog, id) -> exitActivity())
                .show();
    }

    private void exitActivity() {
        this.fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        this.closeActivity();
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

    private void sendSnailUpdateToServer() {
        Activity activity = this;
        DBHelper.updateSnailRunDistance(snailId, totalDistance, distanceFromUser, new DBHelper.GenericCallback() {
            @Override
            public void onSuccess(String message, Integer unused) {
                // activity.runOnUiThread(() -> Toast.makeText(activity, message, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                // runOnUiThread(() -> Toast.makeText(activity, "Failed to update distance", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onBackPressed() {
        endGame();
        this.fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onBackPressed(); // Call the superclass method to handle the default back press behavior
    }

    @Override
    protected void onDestroy() {
        endGame();
        this.fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing()) {
            gameOver();
        }
    }
}