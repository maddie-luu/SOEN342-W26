package com.example.model;
import java.time.*; 
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Task {
    // OCL Constraint: A task cannot have more than 20 subtasks
    private static final int MAX_SUBTASKS = 20;
    
    private String title; 
    private String description;
    private LocalDate createdDate = LocalDate.now();
    private String priorityLevel; 
    private String status = "open"; 
    private ArrayList<Tag> tags = new ArrayList<>();
    private LocalDate duedate; 
    private String collaborator;
    private String collaboratorCategory;
    private final LinkedHashMap<String, String> collaboratorAssignments = new LinkedHashMap<>();

    //recurrence properties
    private String recurrenceType; //"none", "daily", "weekly", "monthly"
    private int recurrenceInterval = 1; //gap between occurrences
    private ArrayList<String> recurrenceWeekdays = new ArrayList<>(); //for weekly recurrence
    private int recurrenceDayOfMonth = -1; //for monthly recurrence (1-31)
    private LocalDate recurrenceStart;
    private LocalDate recurrenceEnd;

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

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(tag.getName());
        }
        return sb.toString();
    }
    
    public LocalDate getDuedate() {
        return duedate;
    }

    public void setDuedate(LocalDate duedate) {
        this.duedate = duedate;
    }

    public String getCollaborator() {
        return collaborator;
    }

    public void setCollaborator(String collaborator) {
        this.collaborator = collaborator;
        if (collaborator == null || collaborator.trim().isEmpty()) {
            return;
        }
        String existingKey = findCollaboratorKey(collaborator);
        String category = collaboratorCategory == null ? "" : collaboratorCategory;
        if (existingKey != null) {
            collaboratorAssignments.put(existingKey, category);
        } else {
            collaboratorAssignments.put(collaborator.trim(), category);
        }
    }

    public String getCollaboratorCategory() {
        return collaboratorCategory;
    }

    public void setCollaboratorCategory(String collaboratorCategory) {
        this.collaboratorCategory = collaboratorCategory;
        if (collaborator == null || collaborator.trim().isEmpty()) {
            return;
        }
        String existingKey = findCollaboratorKey(collaborator);
        if (existingKey != null) {
            collaboratorAssignments.put(existingKey, collaboratorCategory == null ? "" : collaboratorCategory);
        } else {
            collaboratorAssignments.put(collaborator.trim(), collaboratorCategory == null ? "" : collaboratorCategory);
        }
    }

    public void addCollaboratorAssignment(String collaboratorName, String category) {
        if (collaboratorName == null || collaboratorName.trim().isEmpty()) {
            return;
        }
        String normalizedName = collaboratorName.trim();
        String normalizedCategory = category == null ? "" : category.trim();
        String existingKey = findCollaboratorKey(normalizedName);
        if (existingKey != null) {
            collaboratorAssignments.put(existingKey, normalizedCategory);
        } else {
            collaboratorAssignments.put(normalizedName, normalizedCategory);
        }

        if (this.collaborator == null || this.collaborator.trim().isEmpty()) {
            this.collaborator = normalizedName;
            this.collaboratorCategory = normalizedCategory;
        }
    }

    public boolean hasCollaborator(String collaboratorName) {
        return findCollaboratorKey(collaboratorName) != null;
    }

    public Map<String, String> getCollaboratorAssignments() {
        return Collections.unmodifiableMap(collaboratorAssignments);
    }

    public String getCollaboratorsAsString() {
        if (collaboratorAssignments.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : collaboratorAssignments.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(entry.getKey());
            if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                sb.append(" (").append(entry.getValue()).append(")");
            }
        }
        return sb.toString();
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public int getRecurrenceInterval() {
        return recurrenceInterval;
    }

    public void setRecurrenceInterval(int recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval;
    }

    public ArrayList<String> getRecurrenceWeekdays() {
        return recurrenceWeekdays;
    }

    public void setRecurrenceWeekdays(ArrayList<String> recurrenceWeekdays) {
        this.recurrenceWeekdays = recurrenceWeekdays;
    }

    public int getRecurrenceDayOfMonth() {
        return recurrenceDayOfMonth;
    }

    public void setRecurrenceDayOfMonth(int recurrenceDayOfMonth) {
        this.recurrenceDayOfMonth = recurrenceDayOfMonth;
    }

    public LocalDate getRecurrenceStart() {
        return recurrenceStart;
    }

    public void setRecurrenceStart(LocalDate recurrenceStart) {
        this.recurrenceStart = recurrenceStart;
    }

    public LocalDate getRecurrenceEnd() {
        return recurrenceEnd;
    }

    public void setRecurrenceEnd(LocalDate recurrenceEnd) {
        this.recurrenceEnd = recurrenceEnd;
    }

    //Default constructor to create an empty task object.
    public Task() {
    }
    // Constructor with paramters to initialize the task object with the provided values. Description and due date are optional. 
    public Task(String title, String description, String priorityLevel, LocalDate duedate) {
        this.title = title;
        this.description = description;
        this.priorityLevel = priorityLevel;
        this.duedate = duedate;
    }

    public void addSubtask(Subtask subtaskTitle) throws SubtaskLimitExceededException {
        if (this.subtask.size() >= MAX_SUBTASKS) {
            throw new SubtaskLimitExceededException(
                "Cannot add subtask. Task '" + this.title + "' already has " + MAX_SUBTASKS + " subtasks."
            );
        }
        this.subtask.add(subtaskTitle);
    }

    private String findCollaboratorKey(String collaboratorName) {
        if (collaboratorName == null) {
            return null;
        }
        for (String key : collaboratorAssignments.keySet()) {
            if (key.equalsIgnoreCase(collaboratorName.trim())) {
                return key;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Title: " + title + "\n"
             + "Description: " + description + "\n"
             + "Priority: " + priorityLevel + "\n"
             + "Tags: " + getTagsAsString() + "\n"
             + "Due Date: " + duedate + "\n"
             + "Status: " + status + "\n"
             + "Collaborators: " + getCollaboratorsAsString() + "\n"
             + "Recurrence: " + recurrenceType + " (interval=" + recurrenceInterval + ", monthlyDay=" + recurrenceDayOfMonth + ", start=" + recurrenceStart + ", end=" + recurrenceEnd + ")\n"
             + "-----------------------------\n";
    }
}
