package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an activity entry that records actions performed on a Task.
 * Each entry contains a timestamp and a description of the action.
 * Used to maintain a history of all task-related activities.
 */
public class ActivityEntry {
    
    // Timestamp when the activity occurred
    private final LocalDateTime timestamp;
    
    // Description of the action (e.g., "Task created", "Task updated")
    private final String description;
    
    // Formatter for displaying timestamps in a readable format
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Creates a new ActivityEntry with the current timestamp.
     * 
     * @param description Description of the action performed
     */
    public ActivityEntry(String description) {
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    /**
     * Creates a new ActivityEntry with a specific timestamp.
     * Useful for testing or importing historical data.
     * 
     * @param timestamp The timestamp of the activity
     * @param description Description of the action performed
     */
    public ActivityEntry(LocalDateTime timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }

    /**
     * Gets the timestamp of when this activity occurred.
     * @return The activity timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the description of this activity.
     * @return The activity description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a formatted string representation of this activity entry.
     * Format: [timestamp] description
     */
    @Override
    public String toString() {
        return "[" + timestamp.format(FORMATTER) + "] " + description;
    }
}
