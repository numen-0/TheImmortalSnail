package com.example.theimmortalsnail.models;

import java.util.Locale;

public class HistoryEntry {

    private final String name;
    private final String time;
    private final long distance;
    private final long maxDistance;

    public HistoryEntry(String name, String time, long distance, long maxDistance) {
        this.name = name;
        this.time = time;
        this.distance = distance;
        this.maxDistance = maxDistance;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getDistance() {
        return roundDistance(distance);
    }

    public String getMaxDistance() {
        return roundDistance(maxDistance);
    }

    private String roundDistance(long d) {
        if (d >= 1000) {
            float km = d / 1000f;
            return String.format(Locale.US, "%.1fkm", km);
        } else {
            return d + "m";
        }
    }
}
