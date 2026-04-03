package com.example.gateway;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.TaskExportGateway;
import com.example.model.Project;
import com.example.model.Subtask;
import com.example.model.Task;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

public class ICalTaskExporter implements TaskExportGateway {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public String exportTask(Task task, Project project, String outputFilePath) throws IOException {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        if (task.getDuedate() == null) {
            throw new IllegalArgumentException("Only tasks with due dates can be exported.");
        }

        Path outputPath = buildOutputPath(outputFilePath, task.getTitle(), "task");
        String icsContent = buildCalendarContent(singleTaskList(task), project);
        return writeCalendar(outputPath, icsContent);
    }

    @Override
    public String exportProjectTasks(Project project, String outputFilePath) throws IOException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null.");
        }

        String projectTitle = project.getTitle();
        String defaultProjectFileBase = (projectTitle == null ? "project_tasks" : projectTitle + "_tasks");
        Path outputPath = buildOutputPath(outputFilePath, defaultProjectFileBase, "project_tasks");

        List<Task> dueDatedTasks = new ArrayList<>();
        for (Task task : project.getTasks()) {
            if (task != null && task.getDuedate() != null) {
                dueDatedTasks.add(task);
            }
        }

        String icsContent = buildCalendarContent(dueDatedTasks, project);
        return writeCalendar(outputPath, icsContent);
    }

    private String writeCalendar(Path outputPath, String calendarContent) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputPath, calendarContent, StandardCharsets.UTF_8);
        return outputPath.toAbsolutePath().toString();
    }

    private Path buildOutputPath(String outputFilePath, String rawName, String fallbackBase) {
        if (outputFilePath == null || outputFilePath.trim().isEmpty()) {
            String safeTitle = rawName == null ? fallbackBase : rawName.trim().replaceAll("[^a-zA-Z0-9-_]+", "_");
            if (safeTitle.isEmpty()) {
                safeTitle = fallbackBase;
            }
            return Paths.get(safeTitle + ".ics");
        }

        String normalized = outputFilePath.trim();
        if (!normalized.toLowerCase().endsWith(".ics")) {
            normalized = normalized + ".ics";
        }
        return Paths.get(normalized);
    }

    private String buildCalendarContent(List<Task> tasks, Project project) {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//SOEN342//TaskManagementCLI//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        if (tasks != null) {
            for (Task task : tasks) {
                if (task == null || task.getDuedate() == null) {
                    continue;
                }
                calendar.getComponents().add(buildTaskEntry(task, project));
            }
        }

        return serializeCalendar(calendar);
    }

    private VToDo buildTaskEntry(Task task, Project project) {
        VToDo todo = new VToDo();
        todo.getProperties().add(new Uid(UUID.randomUUID() + "@taskmanagementcli"));
        todo.getProperties().add(new Summary(safeText(task.getTitle())));
        todo.getProperties().add(new Description(buildDescription(task)));
        todo.getProperties().add(new Due(toICalDate(task.getDuedate())));
        todo.getProperties().add(new Status(normalizeStatus(task.getStatus())));
        todo.getProperties().add(new Priority(mapPriority(task.getPriorityLevel())));

        String projectName = project == null ? "" : project.getTitle();
        if (projectName != null && !projectName.trim().isEmpty()) {
            todo.getProperties().add(new Categories(projectName));
            todo.getProperties().add(new XProperty("X-PROJECT-NAME", projectName));
        }

        return todo;
    }

    private List<Task> singleTaskList(Task task) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);
        return tasks;
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

        return description.toString();
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

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private Date toICalDate(LocalDate localDate) {
        try {
            return new Date(formatDate(localDate));
        } catch (ParseException e) {
            throw new IllegalStateException("Failed to convert due date to iCalendar date", e);
        }
    }

    private String serializeCalendar(Calendar calendar) {
        try (StringWriter writer = new StringWriter()) {
            CalendarOutputter outputter = new CalendarOutputter(false);
            outputter.output(calendar, writer);
            return normalizeSerializedContent(writer.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate iCalendar content", e);
        }
    }

    private String normalizeSerializedContent(String content) {
        // Keep output stable for current CLI/tests while generating content via iCal4j.
        return content
                .replace("\r\n ", "")
                .replace("\n ", "")
                .replace("\r\n\t", "")
            .replace("\n\t", "");
    }

    @Override
    public String exportFilteredTasks(List<Task> filteredTasks, Map<Task, Project> taskToProject, String outputFilePath) throws IOException {
        if (filteredTasks == null || filteredTasks.isEmpty()) {
            throw new IllegalArgumentException("No tasks to export.");
        }

        Path outputPath = buildOutputPath(outputFilePath, "filtered_tasks", "filtered_tasks");

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//SOEN342//TaskManagementCLI//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        for (Task task : filteredTasks) {
            if (task == null || task.getDuedate() == null) {
                continue;
            }
            Project project = taskToProject == null ? null : taskToProject.get(task);
            calendar.getComponents().add(buildTaskEntry(task, project));
        }

        return writeCalendar(outputPath, serializeCalendar(calendar));
    }
}
