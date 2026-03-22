package com.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing tasks and their activity history.
 * All task operations (create, update, complete, cancel) go through this class
 * to ensure activity entries are automatically recorded.
 * 
 * Follows Single Responsibility Principle - handles task business logic separately from UI.
 */
public class TaskService {

    // In-memory storage for tasks (Proof of Concept)
    private final List<Task> tasks;

    /**
     * Constructor that accepts a reference to the task list.
     * @param tasks The list of tasks to operate on
     */
    public TaskService(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Creates a new task and records a "Task created" activity entry.
     * 
     * @param title Task title (required)
     * @param description Task description (optional, can be null)
     * @param priorityLevel Priority level (e.g., "low", "medium", "high")
     * @param dueDate Due date (optional, can be null)
     * @return The newly created task
     */
    public Task createTask(String title, String description, String priorityLevel, LocalDate dueDate) {
        // Validate required fields
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }

        // Create the new task
        Task task = new Task(title, description, priorityLevel, dueDate);
        
        // Record activity entry for task creation
        task.addActivityEntry(new ActivityEntry("Task created: \"" + title + "\""));
        
        // Add to task list
        tasks.add(task);
        
        return task;
    }

    /**
     * Updates an existing task's fields and records a "Task updated" activity entry.
     * Only non-null parameters will be updated.
     * 
     * @param taskId The ID of the task to update
     * @param newTitle New title (null to keep existing)
     * @param newDescription New description (null to keep existing)
     * @param newPriorityLevel New priority level (null to keep existing)
     * @param newDueDate New due date (null to keep existing)
     * @return true if task was found and updated, false if task not found
     */
    public boolean updateTask(int taskId, String newTitle, String newDescription, 
                              String newPriorityLevel, LocalDate newDueDate) {
        Task task = findTaskById(taskId);
        
        if (task == null) {
            return false; // Task not found
        }

        // Build description of what was updated
        StringBuilder changes = new StringBuilder();
        
        // Update title if provided
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            String oldTitle = task.getTitle();
            task.setTitle(newTitle);
            changes.append("title changed from \"").append(oldTitle).append("\" to \"").append(newTitle).append("\"; ");
        }
        
        // Update description if provided
        if (newDescription != null) {
            task.setDescription(newDescription);
            changes.append("description updated; ");
        }
        
        // Update priority level if provided
        if (newPriorityLevel != null && !newPriorityLevel.trim().isEmpty()) {
            String oldPriority = task.getPriorityLevel();
            task.setPriorityLevel(newPriorityLevel);
            changes.append("priority changed from \"").append(oldPriority).append("\" to \"").append(newPriorityLevel).append("\"; ");
        }
        
        // Update due date if provided
        if (newDueDate != null) {
            LocalDate oldDueDate = task.getDuedate();
            task.setDuedate(newDueDate);
            changes.append("due date changed from \"").append(oldDueDate).append("\" to \"").append(newDueDate).append("\"; ");
        }

        // Only record activity if something was actually changed
        if (changes.length() > 0) {
            task.addActivityEntry(new ActivityEntry("Task updated: " + changes.toString().trim()));
        }

        return true;
    }

    /**
     * Marks a task as completed and records a "Task completed" activity entry.
     * 
     * @param taskId The ID of the task to complete
     * @return true if task was found and completed, false if task not found or already completed
     */
    public boolean completeTask(int taskId) {
        Task task = findTaskById(taskId);
        
        if (task == null) {
            return false; // Task not found
        }

        // Check if task is already completed
        if ("completed".equalsIgnoreCase(task.getStatus())) {
            return false; // Already completed, don't create duplicate entry
        }

        // Check if task is cancelled (cannot complete a cancelled task)
        if ("cancelled".equalsIgnoreCase(task.getStatus())) {
            return false; // Cannot complete a cancelled task
        }

        String oldStatus = task.getStatus();
        task.setStatus("completed");
        
        // Record activity entry
        task.addActivityEntry(new ActivityEntry("Task completed (was: " + oldStatus + ")"));
        
        return true;
    }

    /**
     * Marks a task as cancelled and records a "Task cancelled" activity entry.
     * 
     * @param taskId The ID of the task to cancel
     * @return true if task was found and cancelled, false if task not found or already cancelled
     */
    public boolean cancelTask(int taskId) {
        Task task = findTaskById(taskId);
        
        if (task == null) {
            return false; // Task not found
        }

        // Check if task is already cancelled
        if ("cancelled".equalsIgnoreCase(task.getStatus())) {
            return false; // Already cancelled, don't create duplicate entry
        }

        // Check if task is completed (cannot cancel a completed task)
        if ("completed".equalsIgnoreCase(task.getStatus())) {
            return false; // Cannot cancel a completed task
        }

        String oldStatus = task.getStatus();
        task.setStatus("cancelled");
        
        // Record activity entry
        task.addActivityEntry(new ActivityEntry("Task cancelled (was: " + oldStatus + ")"));
        
        return true;
    }

    /**
     * Gets the activity history for a specific task.
     * 
     * @param taskId The ID of the task
     * @return List of activity entries, or empty list if task not found
     */
    public List<ActivityEntry> getTaskActivityHistory(int taskId) {
        Task task = findTaskById(taskId);
        
        if (task == null) {
            return new ArrayList<>(); // Return empty list if task not found
        }
        
        return task.getActivityHistory();
    }

    /**
     * Finds a task by its ID.
     * 
     * @param taskId The ID to search for
     * @return The task if found, null otherwise
     */
    public Task findTaskById(int taskId) {
        for (Task task : tasks) {
            if (task.getId() == taskId) {
                return task;
            }
        }
        return null;
    }

    /**
     * Gets all tasks.
     * @return List of all tasks
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Prints the activity history for a task to the console.
     * Utility method for displaying history in CLI.
     * 
     * @param taskId The ID of the task
     */
    public void printTaskActivityHistory(int taskId) {
        Task task = findTaskById(taskId);
        
        if (task == null) {
            System.out.println("Task not found with ID: " + taskId);
            return;
        }

        System.out.println("\n=== Activity History for Task #" + taskId + ": \"" + task.getTitle() + "\" ===");
        
        List<ActivityEntry> history = task.getActivityHistory();
        
        if (history.isEmpty()) {
            System.out.println("No activity recorded.");
        } else {
            for (ActivityEntry entry : history) {
                System.out.println(entry.toString());
            }
        }
        
        System.out.println();
    }
}
