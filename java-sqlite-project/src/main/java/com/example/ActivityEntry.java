package com.example;

import java.time.LocalDateTime;

public class ActivityEntry {
    private final LocalDateTime timestamp;
    private final String description;

    public ActivityEntry(LocalDateTime timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return timestamp + " - " + description;
    }
}
