package com.example.theimmortalsnail.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Achievement {
    private String en_desc;
    private String eu_desc;
    private String es_desc;
    private boolean done;

    public Achievement(String en_desc, String eu_desc, String es_desc, boolean done) {
        this.en_desc = en_desc;
        this.eu_desc = eu_desc;
        this.es_desc = es_desc;
        this.done = done;
    }

    public String getDescription(Context context) {
        if ( context == null ) { return en_desc; }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = prefs.getString("language", "en");
        switch (lang) {
            case "es":
                return es_desc;
            case "eu":
                return eu_desc;
            case "en":
            default:
                return en_desc;
        }
    }
    public boolean isDone() {
        return this.done;
    }
}
