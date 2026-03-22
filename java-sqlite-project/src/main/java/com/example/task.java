package com.example;
import java.time.*; 
import java.util.ArrayList;
import java.util.List;

public class Task {
    // Unique identifier for the task
    private int id;
    
    // Static counter to generate unique IDs
    private static int idCounter = 1;
    
    private String title; 
    private String description;
    private LocalDate createdDate = LocalDate.now();
    private String priorityLevel; 
    private String status = "open"; 
    private ArrayList<String> tags = new ArrayList<>();
    private LocalDate duedate; 

    // Activity history for this task - records all actions performed on this task
    private ArrayList<ActivityEntry> activityHistory = new ArrayList<>();

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private ArrayList<Subtask> subtask = new ArrayList<>();
    public ArrayList<Subtask> getSubtask() {
        return subtask;
    }

    public void setSubtask(ArrayList<Subtask> subtask) {
        this.subtask = subtask;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
    
    public LocalDate getDuedate() {
        return duedate;
    }

    public void setDuedate(LocalDate duedate) {
        this.duedate = duedate;
    }

    /**
     * Gets the activity history for this task.
     * @return List of activity entries (unmodifiable view)
     */
    public List<ActivityEntry> getActivityHistory() {
        return new ArrayList<>(activityHistory);
    }

    /**
     * Adds an activity entry to this task's history.
     * Package-private to ensure only TaskService can add entries.
     * 
     * @param entry The activity entry to add
     */
    void addActivityEntry(ActivityEntry entry) {
        this.activityHistory.add(entry);
    }

    //Default constructor to create an empty task object with auto-generated ID.
    public Task() {
        this.id = idCounter++;
    }
    
    // Constructor with parameters to initialize the task object with the provided values.
    public Task(String title, String description, String priorityLevel, LocalDate duedate) {
        this.id = idCounter++;
        this.title = title;
        this.description = description;
        this.priorityLevel = priorityLevel;
        this.duedate = duedate;
    }

    public void addSubtask(Subtask subtaskTitle) {
        this.subtask.add(subtaskTitle);
    }

    @Override
    public String toString() {
        return "Task #" + id + "\n"
             + "Title: " + title + "\n"
             + "Description: " + description + "\n"
             + "Priority: " + priorityLevel + "\n"
             + "Due Date: " + duedate + "\n"
             + "Status: " + status + "\n"
             + "-----------------------------\n";
    }
}
