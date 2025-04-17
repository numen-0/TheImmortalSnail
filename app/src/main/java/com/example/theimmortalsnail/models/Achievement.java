package com.example.theimmortalsnail.models;

public class Achievement {
    public String description;
    public boolean done;

    public Achievement(String description, boolean done) {
        if ( description == null || description.isBlank() ) {
            this.description = "???";
        } else {
            this.description = description;
        }
        this.done = done;
    }
}
