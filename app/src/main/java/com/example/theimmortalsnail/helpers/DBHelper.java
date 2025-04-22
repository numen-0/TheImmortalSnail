package com.example.theimmortalsnail.helpers;

import okhttp3.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.example.theimmortalsnail.models.Achievement;
import com.example.theimmortalsnail.models.SnailRecord;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DBHelper {
    static private Integer userId = null;
    private static final String SERVER_URL = "http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/iduenas003/WEB/";

    @NonNull
    public static Integer getUserId() {
        return DBHelper.userId;
    }
    public static void setUserId(int userId) {
        DBHelper.userId = userId;
    }

    // Fetch finished runs
    public static void getFinishedRuns(Context context, SnailCallback callback) {
        String url = DBHelper.SERVER_URL + "snail.php?user_id=" + DBHelper.userId;
        new SnailFetcher(context, url, callback).execute();
    }

    // Fetch currently active run (if any)
    public static void getSnail(Context context, Integer snailID, SnailSingleCallback callback) {
        String url = DBHelper.SERVER_URL + "snail.php?user_id=" + DBHelper.userId;
        new SnailFetcher(context, url, new SnailCallback() {
            @Override
            public void onResult(List<SnailRecord> records) {
                for (SnailRecord record: records) {
                    if ( record.getId().equals(snailID) ) {
                        callback.onResult(record);
                        return;
                    }
                }
                callback.onResult(null);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }).execute();
    }
    public static void startNewRun(String name, GenericCallback callback) {
        String url = DBHelper.SERVER_URL + "snail.php";
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", DBHelper.userId);
            body.put("name", name);
        } catch (Exception e) {
            callback.onError(e);
            return;
        }
        new PostJsonTask(url, body, new GenericCallback() {
            @Override
            public void onSuccess(String response, Integer unused) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.has("snail_id")) {
                        int snailId = json.getInt("snail_id");
                        callback.onSuccess(json.getString("message"), snailId);
                    } else {
                        callback.onError(new Exception("No snail_id returned"));
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }).execute();
    }


    // End a run (update end_time)
    public static void endRun(int snailId, GenericCallback callback) {
        String url = DBHelper.SERVER_URL + "run_end.php";
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", DBHelper.userId);
            body.put("snail_id", snailId);
        } catch (Exception e) {
            callback.onError(e);
            return;
        }
        new PostJsonTask(url, body, callback).execute();
    }

    // Update distance
    public static void updateSnailRunDistance(int snailId, float distance, float distanceFromUser, GenericCallback callback) {
        String url = DBHelper.SERVER_URL + "run_update.php";
        JSONObject body = new JSONObject();

        try {
            // Pass the required fields (user_id, snail_id, and distance) as JSON
            body.put("user_id", DBHelper.userId);
            body.put("snail_id", snailId);
            body.put("distance", distance);
            body.put("distance_from_user", distanceFromUser);
        } catch (Exception e) {
            callback.onError(e);
            return;
        }

        new PostJsonTask(url, body, new GenericCallback() {
            @Override
            public void onSuccess(String response, Integer unused) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.has("message")) {
                        callback.onSuccess(json.getString("message"), null);
                    } else {
                        callback.onError(new Exception("No message returned"));
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }).execute();
    }

    private static class SnailFetcher extends AsyncTask<Void, Void, List<SnailRecord>> {
        private final Context context;
        private final String url;
        private final DBHelper.SnailCallback callback;

        SnailFetcher(Context context, String url, DBHelper.SnailCallback callback) {
            this.context = context;
            this.url = url;
            this.callback = callback;
        }

        @Override
        protected List<SnailRecord> doInBackground(Void... voids) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                InputStream stream = conn.getInputStream();
                String response = new java.util.Scanner(stream).useDelimiter("\\A").next();
                JSONArray array = new JSONArray(response);

                List<SnailRecord> records = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);

                    int snailId = obj.getInt("id");
                    Bitmap image = fetchSnailImage(snailId);

                    records.add(new SnailRecord(
                            context,
                            snailId,
                            obj.getString("name"),
                            parseDate(obj.getString("start_time")),
                            obj.isNull("end_time") ? null : parseDate(obj.getString("end_time")),
                            (long) obj.getDouble("distance"),
                            (long) obj.getDouble("max_distance"),
                            (long) obj.getDouble("min_distance"),
                            image
                    ));
                }
                return records;

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<SnailRecord> result) {
            if (result != null) {
                callback.onResult(result);
            }
        }
    }

    private static class PostJsonTask extends AsyncTask<Void, Void, String> {
        private final String url;
        private final JSONObject body;
        private final DBHelper.GenericCallback callback;

        PostJsonTask(String url, JSONObject body, DBHelper.GenericCallback callback) {
            this.url = url;
            this.body = body;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                conn.getOutputStream().write(body.toString().getBytes("UTF-8"));

                InputStream stream = conn.getInputStream();
                String response = new java.util.Scanner(stream).useDelimiter("\\A").next();

                return response;
            } catch (Exception e) {
                callback.onError(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                callback.onSuccess(result, null);
            } else {
                callback.onError(new Exception("Empty response"));
            }
        }

    }

    public static void getAchievements(Context context, AchievementCallback callback) {
        String url = DBHelper.SERVER_URL + "achievement.php?user_id=" + DBHelper.userId;

        new AsyncTask<Void, Void, List<Achievement>>() {
            @Override
            protected List<Achievement> doInBackground(Void... voids) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");

                    InputStream stream = conn.getInputStream();
                    String response = new java.util.Scanner(stream).useDelimiter("\\A").next();
                    JSONArray array = new JSONArray(response);

                    List<Achievement> achievements = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        achievements.add(new Achievement(
                                obj.getString("en_desc"),
                                obj.getString("eu_desc"),
                                obj.getString("es_desc"),
                                obj.getBoolean("done")
                        ));
                    }

                    return achievements;

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Achievement> result) {
                if (result != null) {
                    callback.onResult(result);
                }
            }
        }.execute();
    }



    private static Date parseDate(String str) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.parse(str);
    }

    private static Bitmap fetchSnailImage(int snailId) {
        try {
            URL imageUrl = new URL(DBHelper.SERVER_URL + "img.php?snail_id=" + snailId);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();

            if (conn.getResponseCode() == 200 && conn.getContentType().startsWith("image/")) {
                InputStream input = conn.getInputStream();
                return BitmapFactory.decodeStream(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void postSnailImage(int snailId, File imageFile, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("snail_id", String.valueOf(snailId))
                .addFormDataPart(
                        "image",
                        imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/*"))
                )
                .build();

        Request request = new Request.Builder()
                .url(DBHelper.SERVER_URL + "/img.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }


    // Interfaces for callbacks
    public interface SnailCallback {
        void onResult(List<SnailRecord> records);
        void onError(Exception e);
    }

    public interface SnailSingleCallback {
        void onResult(SnailRecord record);
        void onError(Exception e);
    }

    public interface GenericCallback {
        void onSuccess(String message, Integer snailId); // Updated
        void onError(Exception e);
    }

    public interface AchievementCallback {
        void onResult(List<Achievement> achievements);
        void onError(Exception e);
    }
}

/* DB structure
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    password VARCHAR(64) NOT NULL
);

CREATE TABLE snail (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME DEFAULT NULL,
    distance FLOAT DEFAULT 0,
    max_distance FLOAT DEFAULT 0,
    min_distance FLOAT DEFAULT 0,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE achievement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    en_desc VARCHAR(255) NOT NULL,
    eu_desc VARCHAR(255) NOT NULL,
    es_desc VARCHAR(255) NOT NULL
);

CREATE TABLE user_achievements (
    user_id INT,
    achievement_id INT,
    PRIMARY KEY (user_id, achievement_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE
);
*/