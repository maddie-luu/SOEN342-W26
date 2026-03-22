package com.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for exporting tasks to CSV format.
 * Handles file writing and proper CSV formatting.
 * No external libraries used - simple and clean for university project.
 */
public class CSVExporter {

    // CSV column headers in the required order
    private static final String CSV_HEADER = 
        "Task name,description,subtask,status,priority,due date,projectName,projectDescription,collaborator,collaborator category";

    /**
     * Exports a list of tasks to a CSV file.
     * 
     * CSV Format:
     * - Header row with column names
     * - One row per task
     * - Fields separated by commas
     * - Null/missing fields written as "null"
     * - Multiple subtasks concatenated with semicolons
     * 
     * Edge cases handled:
     * - Empty task list → creates file with header only
     * - Invalid file path → throws IOException with message
     * - Null fields → written as "null"
     * - Commas in fields → field wrapped in quotes
     * 
     * @param tasks The list of tasks to export
     * @param filePath The path where the CSV file will be created
     * @throws IOException If file cannot be written
     */
    public static void exportTasksToCSV(List<Task> tasks, String filePath) throws IOException {
        // Use try-with-resources to ensure proper file closing
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            
            // Write the header row
            writer.write(CSV_HEADER);
            writer.newLine();

            // If task list is empty, file will contain only header
            if (tasks == null || tasks.isEmpty()) {
                return;
            }

            // Write each task as a CSV row
            for (Task task : tasks) {
                String csvRow = taskToCSVRow(task);
                writer.write(csvRow);
                writer.newLine();
            }
        }
        // IOException is propagated to caller for handling
    }

    /**
     * Converts a single Task object to a CSV row string.
     * Handles null values and special characters.
     * 
     * @param task The task to convert
     * @return CSV-formatted row string
     */
    private static String taskToCSVRow(Task task) {
        StringBuilder row = new StringBuilder();

        // 1. Task name
        row.append(escapeCSV(task.getTitle()));
        row.append(",");

        // 2. Description (optional)
        row.append(escapeCSV(task.getDescription()));
        row.append(",");

        // 3. Subtasks - concatenate all subtask titles with semicolons
        row.append(escapeCSV(getSubtasksAsString(task.getSubtask())));
        row.append(",");

        // 4. Status
        row.append(escapeCSV(task.getStatus()));
        row.append(",");

        // 5. Priority
        row.append(escapeCSV(task.getPriorityLevel()));
        row.append(",");

        // 6. Due date (optional)
        String dueDate = task.getDuedate() != null ? task.getDuedate().toString() : null;
        row.append(escapeCSV(dueDate));
        row.append(",");

        // 7. Project name (optional)
        String projectName = task.getProject() != null ? task.getProject().getTitle() : null;
        row.append(escapeCSV(projectName));
        row.append(",");

        // 8. Project description (optional)
        String projectDesc = task.getProject() != null ? task.getProject().getDescription() : null;
        row.append(escapeCSV(projectDesc));
        row.append(",");

        // 9. Collaborator name (optional)
        String collabName = task.getCollaborator() != null ? task.getCollaborator().getName() : null;
        row.append(escapeCSV(collabName));
        row.append(",");

        // 10. Collaborator category (optional)
        String collabCategory = task.getCollaborator() != null ? task.getCollaborator().getCategory() : null;
        row.append(escapeCSV(collabCategory));

        return row.toString();
    }

    /**
     * Concatenates subtask titles into a single string.
     * Uses semicolon as delimiter to avoid conflicts with CSV commas.
     * 
     * @param subtasks List of subtasks
     * @return Concatenated string of subtask titles, or null if empty
     */
    private static String getSubtasksAsString(ArrayList<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < subtasks.size(); i++) {
            Subtask subtask = subtasks.get(i);
            if (subtask != null && subtask.getTitle() != null) {
                sb.append(subtask.getTitle());
                // Add semicolon separator between subtasks (not after last one)
                if (i < subtasks.size() - 1) {
                    sb.append("; ");
                }
            }
        }

        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Escapes a field value for CSV format.
     * - Null values become "null"
     * - Values containing commas, quotes, or newlines are wrapped in quotes
     * - Existing quotes are doubled (CSV standard)
     * 
     * @param value The field value to escape
     * @return CSV-safe string
     */
    private static String escapeCSV(String value) {
        // Handle null values
        if (value == null) {
            return "null";
        }

        // Check if value needs to be quoted (contains comma, quote, or newline)
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");

        if (needsQuotes) {
            // Double any existing quotes and wrap in quotes
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }

        return value;
    }
}
