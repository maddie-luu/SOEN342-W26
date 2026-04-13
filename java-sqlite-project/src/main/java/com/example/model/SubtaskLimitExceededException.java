package com.example.model;

/**
 * Exception thrown when attempting to add a subtask to a task that has already reached the 20 subtask limit.
 * OCL Constraint: A task cannot have more than 20 subtasks.
 */
public class SubtaskLimitExceededException extends Exception {
    private static final int MAX_SUBTASKS = 20;

    public SubtaskLimitExceededException() {
        super("Subtask limit reached. A task cannot have more than " + MAX_SUBTASKS + " subtasks.");
    }

    public SubtaskLimitExceededException(String message) {
        super(message);
    }
}
