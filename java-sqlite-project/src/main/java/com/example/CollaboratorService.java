package com.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for managing collaborator-related operations.
 * Centralizes the logic for checking collaborator workload and preventing overload.
 * 
 * OCL Constraint: No collaborator should be overloaded.
 * - Senior: max 2 open tasks
 * - Intermediate: max 5 open tasks
 * - Junior: max 10 open tasks
 */
public class CollaboratorService {

    private final List<Task> tasks;

    /**
     * Constructor that takes a reference to the task list.
     * @param tasks The list of all tasks in the system
     */
    public CollaboratorService(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Counts the number of open tasks currently assigned to a collaborator.
     * Open tasks are those not marked as "completed".
     * 
     * @param collaboratorName The name of the collaborator
     * @return The count of open tasks assigned to the collaborator
     */
    public int getOpenTaskCount(String collaboratorName) {
        if (collaboratorName == null || collaboratorName.trim().isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Task task : tasks) {
            if (collaboratorName.equalsIgnoreCase(task.getCollaborator()) 
                    && !"completed".equalsIgnoreCase(task.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if assigning a new task would overload the collaborator.
     * 
     * @param collaborator The collaborator to check
     * @return true if assigning another task would exceed the limit, false otherwise
     */
    public boolean wouldOverload(Collaborator collaborator) {
        if (collaborator == null || collaborator.getName() == null) {
            return false;
        }
        int currentOpenTasks = getOpenTaskCount(collaborator.getName());
        int limit = collaborator.getOpenTaskLimit();
        return currentOpenTasks >= limit;
    }

    /**
     * Checks if a task can be assigned to a collaborator without causing overload.
     * 
     * @param collaborator The collaborator to check
     * @return true if the task can be assigned, false if it would cause overload
     */
    public boolean canAssignTask(Collaborator collaborator) {
        return !wouldOverload(collaborator);
    }

    /**
     * Attempts to assign a task to a collaborator.
     * Returns an AssignmentResult indicating success or failure with details.
     * 
     * @param task The task to assign
     * @param collaborator The collaborator to assign the task to
     * @return AssignmentResult containing the outcome and any warning message
     */
    public AssignmentResult assignTaskToCollaborator(Task task, Collaborator collaborator) {
        if (task == null) {
            return new AssignmentResult(false, "Task cannot be null.");
        }
        if (collaborator == null) {
            return new AssignmentResult(false, "Collaborator cannot be null.");
        }

        int currentOpenTasks = getOpenTaskCount(collaborator.getName());
        int limit = collaborator.getOpenTaskLimit();

        // Check if assignment would cause overload
        if (currentOpenTasks >= limit) {
            String warningMessage = buildOverloadWarning(collaborator, currentOpenTasks, limit);
            return new AssignmentResult(false, warningMessage);
        }

        // Assignment is allowed - update the task
        task.setCollaborator(collaborator.getName());
        task.setCollaboratorCategory(collaborator.getCategory());

        String successMessage = String.format(
            "Task '%s' successfully assigned to %s (%s). Open tasks: %d/%d",
            task.getTitle(),
            collaborator.getName(),
            collaborator.getCategory(),
            currentOpenTasks + 1,
            limit
        );

        return new AssignmentResult(true, successMessage);
    }

    /**
     * Builds a detailed warning message when a collaborator would be overloaded.
     * 
     * @param collaborator The collaborator
     * @param currentOpenTasks Current number of open tasks
     * @param limit The maximum allowed open tasks
     * @return A formatted warning message
     */
    private String buildOverloadWarning(Collaborator collaborator, int currentOpenTasks, int limit) {
        return String.format(
            "WARNING: Cannot assign task. Collaborator '%s' would be overloaded!\n" +
            "  - Category: %s\n" +
            "  - Current open tasks: %d\n" +
            "  - Maximum allowed: %d\n" +
            "Assignment rejected to maintain balanced workloads.",
            collaborator.getName(),
            collaborator.getCategory(),
            currentOpenTasks,
            limit
        );
    }

    /**
     * Gets a list of all overloaded collaborators from the given list.
     * A collaborator is overloaded if their open task count exceeds their limit.
     * 
     * @param collaborators List of collaborators to check
     * @return List of overloaded collaborators
     */
    public List<Collaborator> getOverloadedCollaborators(List<Collaborator> collaborators) {
        List<Collaborator> overloaded = new ArrayList<>();
        if (collaborators == null) {
            return overloaded;
        }
        for (Collaborator c : collaborators) {
            int openTasks = getOpenTaskCount(c.getName());
            if (openTasks > c.getOpenTaskLimit()) {
                overloaded.add(c);
            }
        }
        return overloaded;
    }

    /**
     * Gets the workload status of a collaborator as a formatted string.
     * 
     * @param collaborator The collaborator to check
     * @return A formatted status string
     */
    public String getWorkloadStatus(Collaborator collaborator) {
        if (collaborator == null) {
            return "Unknown collaborator";
        }
        int openTasks = getOpenTaskCount(collaborator.getName());
        int limit = collaborator.getOpenTaskLimit();
        String status = openTasks >= limit ? "AT CAPACITY" : "Available";
        
        return String.format(
            "%s (%s): %d/%d open tasks [%s]",
            collaborator.getName(),
            collaborator.getCategory(),
            openTasks,
            limit,
            status
        );
    }

    /**
     * Inner class to represent the result of a task assignment attempt.
     */
    public static class AssignmentResult {
        private final boolean success;
        private final String message;

        public AssignmentResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
