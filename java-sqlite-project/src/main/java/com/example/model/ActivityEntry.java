package com.example.model;

import java.time.LocalDateTime;

public class ActivityEntry {
    private final LocalDateTime timestamp;
    private final int taskId;
    private final String description;

    public ActivityEntry(LocalDateTime timestamp, String description) {
        this(timestamp, 0, description);
    }

    public ActivityEntry(LocalDateTime timestamp, int taskId, String description) {
        this.timestamp = timestamp;
        this.taskId = taskId;
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return timestamp + " - " + description;
    }
}
