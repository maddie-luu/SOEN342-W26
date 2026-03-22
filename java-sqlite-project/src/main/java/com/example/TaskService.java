package com.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service class for task-related business logic.
 * Follows Single Responsibility Principle - handles task operations separately from UI.
 * This class operates on in-memory task storage (Proof of Concept).
 */
public class TaskService {

    // Reference to the task list (in-memory storage for PoC)
    private final List<Task> tasks;

    /**
     * Constructor that accepts a reference to the task list.
     * @param tasks The list of tasks to operate on
     */
    public TaskService(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Searches for tasks using a keyword found in the task title or description.
     * 
     * Search behavior:
     * - Case-insensitive matching
     * - Partial matches supported (e.g., "proj" matches "project")
     * - Searches BOTH title AND description fields
     * 
     * Edge cases:
     * - If keyword is null or empty → returns all OPEN tasks sorted by due date
     * - If no tasks match → returns an empty list
     * 
     * @param keyword The search keyword to match against title and description
     * @return List of matching tasks (may be empty)
     */
    public List<Task> searchTasks(String keyword) {
        // Edge case: null or empty keyword → return all OPEN tasks sorted by due date
        if (keyword == null || keyword.trim().isEmpty()) {
            return getOpenTasksSortedByDueDate();
        }

        // Convert keyword to lowercase for case-insensitive matching
        String lowerKeyword = keyword.toLowerCase().trim();

        // Filter tasks where title OR description contains the keyword
        List<Task> matchingTasks = new ArrayList<>();
        
        for (Task task : tasks) {
            if (matchesKeyword(task, lowerKeyword)) {
                matchingTasks.add(task);
            }
        }

        return matchingTasks;
    }

    /**
     * Checks if a task's title or description contains the keyword.
     * Handles null values safely.
     * 
     * @param task The task to check
     * @param lowerKeyword The keyword in lowercase
     * @return true if title or description contains the keyword
     */
    private boolean matchesKeyword(Task task, String lowerKeyword) {
        // Check title (title should not be null, but handle it safely)
        String title = task.getTitle();
        if (title != null && title.toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // Check description (description is optional, may be null)
        String description = task.getDescription();
        return description != null && description.toLowerCase().contains(lowerKeyword);
    }

    /**
     * Returns all tasks with "open" status, sorted by due date.
     * Tasks with null due dates are placed at the end.
     * 
     * @return List of open tasks sorted by due date (ascending)
     */
    private List<Task> getOpenTasksSortedByDueDate() {
        List<Task> openTasks = new ArrayList<>();

        // Filter for open tasks only
        for (Task task : tasks) {
            if ("open".equalsIgnoreCase(task.getStatus())) {
                openTasks.add(task);
            }
        }

        // Sort by due date (nulls last)
        openTasks.sort(Comparator.comparing(
            Task::getDuedate,
            Comparator.nullsLast(Comparator.naturalOrder())
        ));

        return openTasks;
    }

    /**
     * Returns all tasks in the system.
     * @return List of all tasks
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Returns the count of tasks matching the keyword.
     * Utility method for displaying search result summary.
     * 
     * @param keyword The search keyword
     * @return Number of matching tasks
     */
    public int countMatchingTasks(String keyword) {
        return searchTasks(keyword).size();
    }
}
