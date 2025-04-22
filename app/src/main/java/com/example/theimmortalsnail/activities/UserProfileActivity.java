package com.example.theimmortalsnail.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.theimmortalsnail.R;
import com.example.theimmortalsnail.fragments.AchievementsFragment;
import com.example.theimmortalsnail.fragments.HistoryFragment;
import com.example.theimmortalsnail.helpers.DBHelper;
import com.example.theimmortalsnail.models.SnailRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserProfileActivity extends BaseActivity implements HistoryFragment.OnSnailSelectedListener {
    static final int REQUEST_IMAGE_CAPTURE = 1020;
    private static final int REQUEST_PERMISSION_CAMERA = 1021;
    Uri photoUri;
    File photoFile;
    private boolean isHistoryShowing;
    private ImageView profilePic;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Integer snailId;


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

        updateProfile();

        // run
        cameraLauncher = registerForActivityResult(new
                ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bitmap thumbnail = (Bitmap) result.getData().getExtras().get("data");
                try {
                    photoFile = createTempImageFile();
                    OutputStream os = new FileOutputStream(photoFile);
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();

                    // Update the ImageView with the captured image
                    photoUri = Uri.fromFile(photoFile);
                    profilePic.setImageURI(photoUri);

                    // Upload the image
                    uploadImageToServer(photoFile);
                    if ( !isHistoryShowing ) {
                        Fragment fragment;
                        fragment = new HistoryFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.profileFragmentContainer, fragment)
                                .commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        this.profilePic = findViewById(R.id.profilePic);
        profilePic.setOnClickListener(v -> {
            if (snailId == null) {
                Toast.makeText(getApplicationContext(), "select a snail first", Toast.LENGTH_SHORT).show();
                return;
            }
            System.out.println("CLICK!!!!!!!!!");
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            }
        });
    }

    private void updateProfile() {
        if ( snailId == null ) {
            nullProfile();
            return;
        }

        DBHelper.getSnail(this, snailId, new DBHelper.SnailSingleCallback() {
            @Override
            public void onResult(SnailRecord record) {
                if (record == null) {
                    nullProfile();
                } else {
                    setProfile(record);
                }
            }

            @Override
            public void onError(Exception e) {
                System.out.println("LOGIN: error in fetch");
                nullProfile();
            }
        });

    }

    private void nullProfile() {
        ((ImageView) this.findViewById(R.id.profilePic)).setImageDrawable(getDrawable(R.drawable.snail));
        ((TextView) this.findViewById(R.id.snailName)).setText("???");
        ((TextView) this.findViewById(R.id.timeVal)).setText("???");
        ((TextView) this.findViewById(R.id.distanceVal)).setText("???");
        ((TextView) this.findViewById(R.id.maxDistanceVal)).setText("???");
        ((TextView) this.findViewById(R.id.minDistanceVal)).setText("???");
    }

    private void setProfile(SnailRecord record) {
        ((ImageView) this.findViewById(R.id.profilePic)).setImageBitmap(record.getImg());
        ((TextView) this.findViewById(R.id.snailName)).setText(record.getName());
        ((TextView) this.findViewById(R.id.timeVal)).setText(record.getTime());
        ((TextView) this.findViewById(R.id.distanceVal)).setText(record.getDistance());
        ((TextView) this.findViewById(R.id.maxDistanceVal)).setText(record.getMaxDistance());
        ((TextView) this.findViewById(R.id.minDistanceVal)).setText(record.getMinDistance());
    }

    private void launchCamera() {
        Intent elIntentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(elIntentFoto);
    }

    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                timeStamp + "_profile_",
                ".jpg",
                storageDir
        );
    }

    private void uploadImageToServer(File photoFile) {
        DBHelper.postSnailImage(snailId, photoFile, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Upload: Upload failed " + e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "null";

                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Image uploaded!", Toast.LENGTH_SHORT).show());
                    if ( !isHistoryShowing ) {
                        Fragment fragment;
                        fragment = new HistoryFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.profileFragmentContainer, fragment)
                                .commit();
                    }
                    updateProfile();
                } else {
                    System.out.println("Upload: Upload failed with code " + response.code() + ": " + body);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Show it immediately
            profilePic.setImageURI(photoUri);

            // Upload it (replace 3 with the actual snail/user ID)
            DBHelper.postSnailImage(snailId, photoFile, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show());
                    if ( !isHistoryShowing ) {
                        Fragment fragment;
                        fragment = new HistoryFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.profileFragmentContainer, fragment)
                                .commit();
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Image uploaded!", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "need camera perms...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void snailUpdate(SnailRecord snail) {
        System.out.println("Selected snail: " + snail.getName());
        snailId = snail.getId();
        this.setProfile(snail);
    }
}