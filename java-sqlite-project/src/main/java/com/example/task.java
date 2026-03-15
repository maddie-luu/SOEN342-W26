package com.example;
import java.time.*; 
import java.util.ArrayList;

public class task {
    private String title; 
    private String description;
    private LocalDate createdDate = LocalDate.now();
    private String priorityLevel; 
    private String status = "open"; 
    private ArrayList<String> tags = new ArrayList<>();
    private LocalDate duedate; 

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

    //Default constructor to create an empty task object.
    public task() {
    }
    // Constructor with paramters to initialize the task object with the provided values. Description and due date are optional. 
    public task(String title, String description, String priorityLevel, LocalDate duedate) {
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
        return "Title: " + title + "\n"
             + "Description: " + description + "\n"
             + "Priority: " + priorityLevel + "\n"
             + "Due Date: " + duedate + "\n"
             + "Status: " + status + "\n"
             + "-----------------------------\n";
    }
}
