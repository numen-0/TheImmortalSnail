package com.example.theimmortalsnail.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.theimmortalsnail.R;

import java.util.Date;
import java.util.Locale;

public class SnailRecord {

    private final Integer id;
    private final String name;
    private final Date startTime;
    private final Date endTime;
    private final long distance;
    private final long maxDistance;
    private final long minDistance;
    private final Bitmap img;

    public SnailRecord(Context context, Integer id, String name, Date startTime, Date endTime, long distance, long maxDistance, long minDistance, Bitmap img) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.distance = distance;
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
        this.img = (img != null) ? img : getDefaultImage(context);
    }

    private Bitmap getDefaultImage(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.snail);
    }

    public Integer getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }

    public Bitmap getImg() {
        return this.img;
    }

    public String getTime() {
        Date end = (endTime != null) ? endTime : new Date();
        long durationMillis = end.getTime() - startTime.getTime();

        long seconds = (durationMillis / 1000) % 60;
        long minutes = (durationMillis / (1000 * 60)) % 60;
        long hours = (durationMillis / (1000 * 60 * 60));

        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String getDistance() {
        return roundDistance(distance);
    }

    public String getMaxDistance() {
        return roundDistance(maxDistance);
    }

    public String getMinDistance() {
        return roundDistance(minDistance);
    }

    public static String roundDistance(double d) {
        if (d >= 1000.0d) {
            return String.format(Locale.US, "%.1fkm", d / 1000.0d);
        } else {
            return String.format(Locale.US, "%.1fm", d);
        }
    }
    public static String roundDistance(long d) {
        if (d >= 1000) {
            float km = d / 1000f;
            return String.format(Locale.US, "%.1fkm", km);
        } else {
            return d + "m";
        }
    }

    public boolean isActive() {
        return this.minDistance == 0;
    }
}
