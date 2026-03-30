package com.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ICalTaskExporter implements TaskExportGateway {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    @Override
    public String exportTask(Task task, Project project, String outputFilePath) throws IOException {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        if (task.getDuedate() == null) {
            throw new IllegalArgumentException("Only tasks with due dates can be exported.");
        }

        Path outputPath = buildOutputPath(outputFilePath, task);
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String icsContent = buildICalContent(task, project);
        Files.writeString(outputPath, icsContent, StandardCharsets.UTF_8);
        return outputPath.toAbsolutePath().toString();
    }

    private Path buildOutputPath(String outputFilePath, Task task) {
        if (outputFilePath == null || outputFilePath.trim().isEmpty()) {
            String safeTitle = task.getTitle() == null ? "task" : task.getTitle().trim().replaceAll("[^a-zA-Z0-9-_]+", "_");
            if (safeTitle.isEmpty()) {
                safeTitle = "task";
            }
            return Paths.get(safeTitle + ".ics");
        }

        String normalized = outputFilePath.trim();
        if (!normalized.toLowerCase().endsWith(".ics")) {
            normalized = normalized + ".ics";
        }
        return Paths.get(normalized);
    }

    private String buildICalContent(Task task, Project project) {
        StringBuilder builder = new StringBuilder();
        builder.append("BEGIN:VCALENDAR\r\n");
        builder.append("VERSION:2.0\r\n");
        builder.append("PRODID:-//SOEN342//TaskManagementCLI//EN\r\n");
        builder.append("CALSCALE:GREGORIAN\r\n");
        builder.append("BEGIN:VTODO\r\n");
        builder.append("UID:").append(UUID.randomUUID()).append("@taskmanagementcli\r\n");
        builder.append("DTSTAMP:").append(LocalDateTime.now().format(DATE_TIME_FORMAT)).append("\r\n");
        builder.append("SUMMARY:").append(escapeICalText(task.getTitle())).append("\r\n");
        builder.append("DESCRIPTION:").append(buildDescription(task)).append("\r\n");
        builder.append("DUE;VALUE=DATE:").append(formatDate(task.getDuedate())).append("\r\n");
        builder.append("STATUS:").append(normalizeStatus(task.getStatus())).append("\r\n");
        builder.append("PRIORITY:").append(mapPriority(task.getPriorityLevel())).append("\r\n");

        String projectName = project == null ? "" : project.getTitle();
        if (projectName != null && !projectName.trim().isEmpty()) {
            builder.append("CATEGORIES:").append(escapeICalText(projectName)).append("\r\n");
            builder.append("X-PROJECT-NAME:").append(escapeICalText(projectName)).append("\r\n");
        }

        builder.append("END:VTODO\r\n");
        builder.append("END:VCALENDAR\r\n");
        return builder.toString();
    }

    private String buildDescription(Task task) {
        StringBuilder description = new StringBuilder();
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            description.append(task.getDescription().trim());
        } else {
            description.append("No description");
        }

        List<Subtask> subtasks = task.getSubtask();
        if (subtasks != null && !subtasks.isEmpty()) {
            description.append("\\nSubtasks:");
            for (Subtask subtask : subtasks) {
                String title = subtask == null ? "" : subtask.getTitle();
                if (title == null || title.trim().isEmpty()) {
                    continue;
                }
                description.append("\\n- ").append(title.trim());
            }
        }

        return escapeICalText(description.toString());
    }

    private String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "NEEDS-ACTION";
        }
        String normalized = status.trim().toLowerCase();
        switch (normalized) {
            case "completed":
                return "COMPLETED";
            case "in progress":
                return "IN-PROCESS";
            case "cancelled":
            case "canceled":
                return "CANCELLED";
            default:
                return "NEEDS-ACTION";
        }
    }

    private int mapPriority(String priorityLevel) {
        if (priorityLevel == null) {
            return 5;
        }
        switch (priorityLevel.trim().toLowerCase()) {
            case "high":
                return 1;
            case "low":
                return 9;
            default:
                return 5;
        }
    }

    private String escapeICalText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}
