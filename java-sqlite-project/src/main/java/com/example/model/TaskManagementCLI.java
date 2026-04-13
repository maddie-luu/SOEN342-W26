package com.example.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.example.CollaboratorService;
import com.example.TaskExportGateway;
import com.example.persistence.TaskDAO;
/**
 * Simple command-line interface for task and project management.
 * This is the first step: providing the user menu and
 * wiring up placeholders for the required actions.
 */
public class TaskManagementCLI {

    private final Scanner scanner;
    public final ArrayList<Task> tasks = new ArrayList<>();    
    public final ArrayList<Project> projects = new ArrayList<>();
    private final ArrayList<ActivityEntry> activityHistory = new ArrayList<>();
    private final CollaboratorService collaboratorService;
    private final TaskExportGateway taskExportGateway;

    public TaskManagementCLI(TaskExportGateway taskExportGateway) {
        this.scanner = new Scanner(System.in);
        this.collaboratorService = new CollaboratorService(tasks);
        this.taskExportGateway = taskExportGateway;
    }

    public void run() {
        boolean exitRequested = false;

        while (!exitRequested) {
            printMenu();
            int choice = readInt("Select an option: ");

            switch (choice) {
                case 1:
                    createTask();
                    break;
                case 2:
                    createProject();
                    break;
                case 3:
                    assignTaskToProject();
                    break;
                case 4:
                    editTask();
                    break;
                case 5:
                    editProject();
                    break;
                case 6:
                    viewTasks();
                    break;
                case 7:
                    searchTasks();
                    break;
                case 8:
                    viewTaskHistory();
                    break;
                case 9:
                    importTasksFromCSV();
                    break;
                case 10:
                    viewOverloadedCollaborators();
                    break;
                case 11:
                    exportSingleTaskToICal();
                    break;
                case 12:
                    exportProjectTasksToICal();
                    break;
                case 13:
                    exportFilteredTasksToICal();
                    break;
                case 14:
                    System.out.println("Exiting application. Goodbye!");
                    exitRequested = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose a number from the menu.");
            }

            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=============================");
        System.out.println("       Task Management       ");
        System.out.println("=============================");
        System.out.println("1. Create task");
        System.out.println("2. Create project");
        System.out.println("3. Assign task to project");
        System.out.println("4. Edit task");
        System.out.println("5. Edit project");
        System.out.println("6. View tasks");
        System.out.println("7. Search tasks");
        System.out.println("8. History of task-related activities");
        System.out.println("9. Import tasks from CSV");
        System.out.println("10. View overloaded collaborators");
        System.out.println("11. Export single task to iCal (.ics)");
        System.out.println("12. Export project tasks to iCal (.ics)");
        System.out.println("13. Export filtered tasks to iCal (.ics)");
        System.out.println("14. Exit");
        System.out.println();
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = scanner.nextInt();
                scanner.nextLine(); // consume end of line
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number.");
                scanner.nextLine(); // discard invalid input
            }
        }
    }

    private void createTask() {
        Task newTask = new Task();
        System.out.print("Enter task title: ");
        newTask.setTitle(scanner.nextLine().trim());
        System.out.print("Enter task description: ");
        newTask.setDescription(scanner.nextLine().trim());
        System.out.print("Enter task priority level (low, medium, high): ");
        newTask.setPriorityLevel(scanner.nextLine().trim());

        LocalDate dueDate = null;
        System.out.print("Enter task due date (YYYY-MM-DD): ");
        String dueDateInput = scanner.nextLine().trim();
        try {
            dueDate = LocalDate.parse(dueDateInput);
            newTask.setDuedate(dueDate);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Task will be created without a due date.");
        }

        System.out.print("Assign to project (leave blank for none): ");
        String projectName = scanner.nextLine().trim();
        String projectDescription = "";
        Project project = null;
        if (!projectName.isEmpty()) {
            System.out.print("Enter project description (optional): ");
            projectDescription = scanner.nextLine().trim();
            project = getOrCreateProject(projectName, projectDescription);
        }

        System.out.print("Enter task tags (comma-separated, optional): ");
        String tagsInput = scanner.nextLine().trim();
        if (!tagsInput.isEmpty()) {
            for (String tagName : tagsInput.split(",")) {
                tagName = tagName.trim();
                if (!tagName.isEmpty()) {
                    newTask.addTag(new Tag(tagName));
                }
            }
        }

        System.out.print("Assign external collaborator(s) (comma-separated names, optional): ");
        String collaboratorInput = scanner.nextLine().trim();
        if (!collaboratorInput.isEmpty()) {
            if (project == null) {
                System.out.println("External collaborators must be defined under a project. Assign this task to a project first.");
                return;
            }

            String[] collaboratorNames = collaboratorInput.split(",");
            for (String rawName : collaboratorNames) {
                String collaboratorName = rawName.trim();
                if (collaboratorName.isEmpty()) {
                    continue;
                }
                if (isSelfAssignmentName(collaboratorName)) {
                    continue;
                }
                if (!linkExternalCollaboratorToTask(newTask, project, collaboratorName)) {
                    return;
                }
            }
        }

        System.out.print("Recurrence type (none/daily/weekly/monthly): ");
        String recurrenceType = scanner.nextLine().trim().toLowerCase();
        if (!"none".equals(recurrenceType)
                && !"daily".equals(recurrenceType)
                && !"weekly".equals(recurrenceType)
                && !"monthly".equals(recurrenceType)) {
            recurrenceType = "none";
        }
        newTask.setRecurrenceType(recurrenceType);

        if ("none".equalsIgnoreCase(recurrenceType)) {
            // US-16: enforce open-task-without-due-date limit for non-recurring tasks.
            if (dueDate == null) {
                int openTasksWithoutDueDate = countOpenTasksWithoutDueDate();
                if (openTasksWithoutDueDate >= 50) {
                    System.out.println("ERROR: Cannot create task. Maximum limit of 50 open tasks without due date has been reached.");
                    System.out.println("Please set a due date or complete some existing open tasks.");
                    return;
                }
            }

            if (dueDate != null && !isTaskNameDueDateUnique(newTask.getTitle(), dueDate)) {
                System.out.println("A task with the same name and due date already exists. Cannot create duplicate.");
                return;
            }

            newTask.setRecurrenceStart(dueDate);
            newTask.setRecurrenceEnd(dueDate);
        } else {
            System.out.print("Recurrence start date (YYYY-MM-DD, leave blank to use task due date): ");
            String startDateInput = scanner.nextLine().trim();
            LocalDate recurrenceStart = dueDate;
            if (!startDateInput.isEmpty()) {
                try {
                    recurrenceStart = LocalDate.parse(startDateInput);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid start date format. Recurrence creation cancelled.");
                    return;
                }
            }

            if (recurrenceStart == null) {
                System.out.println("Recurring tasks require a valid start date (or task due date).");
                return;
            }

            newTask.setRecurrenceStart(recurrenceStart);
            newTask.setDuedate(recurrenceStart);

            System.out.print("Recurrence interval (number of units, default 1): ");
            String intInput = scanner.nextLine().trim();
            try {
                int interval = intInput.isEmpty() ? 1 : Integer.parseInt(intInput);
                newTask.setRecurrenceInterval(Math.max(1, interval));
            } catch (NumberFormatException e) {
                newTask.setRecurrenceInterval(1);
            }

            System.out.print("Recurrence end date (YYYY-MM-DD): ");
            String endDateInput = scanner.nextLine().trim();
            if (endDateInput.isEmpty()) {
                System.out.println("Recurrence end date is required for recurring tasks.");
                return;
            }
            try {
                LocalDate endDate = LocalDate.parse(endDateInput);
                if (endDate.isBefore(recurrenceStart)) {
                    System.out.println("Recurrence end date must be on or after the start date.");
                    return;
                }
                newTask.setRecurrenceEnd(endDate);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid end date format. Recurrence creation cancelled.");
                return;
            }

            if ("weekly".equalsIgnoreCase(recurrenceType)) {
                System.out.print("Enter weekdays for recurrence (comma-separated MONDAY,TUESDAY...): ");
                String daysInput = scanner.nextLine().trim();
                Set<DayOfWeek> weekdaySet = parseWeekdaySet(daysInput);
                ArrayList<String> weekdays = new ArrayList<>();
                for (DayOfWeek day : weekdaySet) {
                    weekdays.add(day.name());
                }
                if (weekdays.isEmpty()) {
                    weekdays.add(recurrenceStart.getDayOfWeek().name());
                }
                newTask.setRecurrenceWeekdays(weekdays);
            } else if ("monthly".equalsIgnoreCase(recurrenceType)) {
                System.out.print("Monthly recurrence day (1-31, leave blank to use start date day): ");
                String monthlyDayInput = scanner.nextLine().trim();
                int monthlyDay = recurrenceStart.getDayOfMonth();
                if (!monthlyDayInput.isEmpty()) {
                    try {
                        int parsedDay = Integer.parseInt(monthlyDayInput);
                        if (parsedDay < 1 || parsedDay > 31) {
                            System.out.println("Invalid day of month. Recurrence creation cancelled.");
                            return;
                        }
                        monthlyDay = parsedDay;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid monthly day value. Recurrence creation cancelled.");
                        return;
                    }
                }
                newTask.setRecurrenceDayOfMonth(monthlyDay);
            }

        }

        if ("none".equalsIgnoreCase(newTask.getRecurrenceType())) {
            tasks.add(newTask);
            if (project != null) project.addTask(newTask);
            try {
                TaskDAO.insertTask(newTask);
            } catch (Exception e) {
                System.out.println("Warning: could not save task to database: " + e.getMessage());
            }
            System.out.println("Task created successfully.");
            logActivity("Created task: '" + newTask.getTitle() + "' with due date " + newTask.getDuedate());
        } else {
            List<Task> occurrences = generateRecurringTasks(newTask);
            for (Task occurrence : occurrences) {
                if (occurrence.getDuedate() != null && isTaskNameDueDateUnique(occurrence.getTitle(), occurrence.getDuedate())) {
                    tasks.add(occurrence);
                    if (project != null) project.addTask(occurrence);
                    try {
                        TaskDAO.insertTask(occurrence);
                    } catch (Exception e) {
                        System.out.println("Warning: could not save task to database: " + e.getMessage());
                    }
                }
            }
            logActivity("Created recurring task: '" + newTask.getTitle() + "' (" + occurrences.size() + " occurrences)");
        }
    }


    private void createProject() {
        Project project = new Project();
        System.out.print("Enter project title: ");
        project.setTitle(scanner.nextLine());
        System.out.print("Enter project description: ");
        project.setDescription(scanner.nextLine());
        System.out.println("Project created successfully.");
        projects.add(project);
    }

    private List<Task> generateRecurringTasks(Task baseTask) {
        List<Task> occurrences = new ArrayList<>();
        if (baseTask.getRecurrenceStart() == null || baseTask.getRecurrenceEnd() == null) {
            return occurrences;
        }
        LocalDate start = baseTask.getRecurrenceStart();
        LocalDate end = baseTask.getRecurrenceEnd();
        String type = baseTask.getRecurrenceType() == null ? "none" : baseTask.getRecurrenceType().toLowerCase();
        int interval = Math.max(1, baseTask.getRecurrenceInterval());

        switch (type) {
            case "daily":
                for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(interval)) {
                    occurrences.add(buildOccurrenceTask(baseTask, current));
                }
                break;
            case "weekly":
                Set<DayOfWeek> selectedWeekdays = parseWeekdaySet(baseTask.getRecurrenceWeekdays(), start.getDayOfWeek());
                LocalDate weeklyAnchor = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
                    long weeksSinceStart = ChronoUnit.WEEKS.between(
                            weeklyAnchor,
                            current.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    );
                    if (weeksSinceStart % interval == 0 && selectedWeekdays.contains(current.getDayOfWeek())) {
                        occurrences.add(buildOccurrenceTask(baseTask, current));
                    }
                }
                break;
            case "monthly":
                int monthlyDay = baseTask.getRecurrenceDayOfMonth() > 0
                        ? baseTask.getRecurrenceDayOfMonth()
                        : start.getDayOfMonth();
                LocalDate monthCursor = start.withDayOfMonth(1);
                LocalDate monthAnchor = start.withDayOfMonth(1);
                while (!monthCursor.isAfter(end)) {
                    long monthsSinceStart = ChronoUnit.MONTHS.between(monthAnchor, monthCursor);
                    if (monthsSinceStart % interval == 0 && monthlyDay <= monthCursor.lengthOfMonth()) {
                        LocalDate occurrenceDate = monthCursor.withDayOfMonth(monthlyDay);
                        if (!occurrenceDate.isBefore(start) && !occurrenceDate.isAfter(end)) {
                            occurrences.add(buildOccurrenceTask(baseTask, occurrenceDate));
                        }
                    }
                    monthCursor = monthCursor.plusMonths(1);
                }
                break;
            default:
                if (!start.isAfter(end)) {
                    occurrences.add(buildOccurrenceTask(baseTask, start));
                }
                break;
        }

        return occurrences;
    }

    private Task buildOccurrenceTask(Task baseTask, LocalDate dueDate) {
        Task occurrence = new Task();
        occurrence.setTitle(baseTask.getTitle());
        occurrence.setDescription(baseTask.getDescription());
        occurrence.setPriorityLevel(baseTask.getPriorityLevel());
        occurrence.setStatus(baseTask.getStatus());
        occurrence.setCollaborator(baseTask.getCollaborator());
        occurrence.setCollaboratorCategory(baseTask.getCollaboratorCategory());
        occurrence.setRecurrenceType(baseTask.getRecurrenceType());
        occurrence.setRecurrenceInterval(baseTask.getRecurrenceInterval());
        occurrence.setRecurrenceStart(baseTask.getRecurrenceStart());
        occurrence.setRecurrenceEnd(baseTask.getRecurrenceEnd());
        occurrence.setRecurrenceDayOfMonth(baseTask.getRecurrenceDayOfMonth());
        occurrence.setDuedate(dueDate);

        ArrayList<Tag> copiedTags = new ArrayList<>();
        if (baseTask.getTags() != null) {
            for (Tag tag : baseTask.getTags()) {
                if (tag != null) {
                    copiedTags.add(new Tag(tag.getName()));
                }
            }
        }
        occurrence.setTags(copiedTags);

        ArrayList<Subtask> copiedSubtasks = new ArrayList<>();
        if (baseTask.getSubtask() != null) {
            for (Subtask subtask : baseTask.getSubtask()) {
                if (subtask != null) {
                    Subtask subtaskCopy = new Subtask(subtask.getTitle(), subtask.getDescription());
                    subtaskCopy.setStatus(subtask.getStatus());
                    copiedSubtasks.add(subtaskCopy);
                }
            }
        }
        occurrence.setSubtask(copiedSubtasks);

        ArrayList<String> weekdaysCopy = new ArrayList<>();
        if (baseTask.getRecurrenceWeekdays() != null) {
            weekdaysCopy.addAll(baseTask.getRecurrenceWeekdays());
        }
        occurrence.setRecurrenceWeekdays(weekdaysCopy);

        return occurrence;
    }

    private Set<DayOfWeek> parseWeekdaySet(String daysInput) {
        Set<DayOfWeek> weekdays = new HashSet<>();
        if (daysInput == null || daysInput.trim().isEmpty()) {
            return weekdays;
        }
        for (String day : daysInput.split(",")) {
            String normalized = day.trim().toUpperCase();
            if (normalized.isEmpty()) {
                continue;
            }
            try {
                weekdays.add(DayOfWeek.valueOf(normalized));
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid weekday entries provided by user input.
            }
        }
        return weekdays;
    }

    private Set<DayOfWeek> parseWeekdaySet(List<String> dayNames, DayOfWeek fallbackDay) {
        Set<DayOfWeek> weekdays = new HashSet<>();
        if (dayNames != null) {
            for (String dayName : dayNames) {
                if (dayName == null) {
                    continue;
                }
                try {
                    weekdays.add(DayOfWeek.valueOf(dayName.trim().toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                    // Ignore invalid values and keep parsing the rest.
                }
            }
        }
        if (weekdays.isEmpty() && fallbackDay != null) {
            weekdays.add(fallbackDay);
        }
        return weekdays;
    }

    private void assignTaskToProject() {
        if (tasks.isEmpty() || projects.isEmpty()) {
            System.out.println("Tasks or projects are not available to assign.");
            return;
        }

        System.out.println("Choose task to assign:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ". " + tasks.get(i).getTitle());
        }
        int taskNumber = readInt("Enter task number: ");
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            System.out.println("Invalid task number.");
            return;
        }
        Task selectedTask = tasks.get(taskNumber - 1);

        Project existingProject = findProjectForTask(selectedTask);
        if (existingProject != null) {
            System.out.println("Task '" + selectedTask.getTitle() + "' is already in project '" + existingProject.getTitle() + "'.");
            System.out.println("Use Edit Task (option 4) > Edit associated project to move it.");
            return;
        }

        System.out.println("Choose project:");
        for (int i = 0; i < projects.size(); i++) {
            System.out.println((i + 1) + ". " + projects.get(i).getTitle());
        }
        int projectNumber = readInt("Enter project number: ");
        if (projectNumber < 1 || projectNumber > projects.size()) {
            System.out.println("Invalid project number.");
            return;
        }
        Project project = projects.get(projectNumber - 1);

        System.out.print("Assign additional external collaborator(s) (comma-separated names, optional): ");
        String collaboratorInput = scanner.nextLine().trim();
        if (!collaboratorInput.isEmpty()) {
            String[] collaboratorNames = collaboratorInput.split(",");
            for (String rawName : collaboratorNames) {
                String collaboratorName = rawName.trim();
                if (collaboratorName.isEmpty()) {
                    continue;
                }
                if (isSelfAssignmentName(collaboratorName)) {
                    continue;
                }
                if (!linkExternalCollaboratorToTask(selectedTask, project, collaboratorName)) {
                    return;
                }
            }
        }

        project.addTask(selectedTask);
        logActivity("Assigned task " + selectedTask.getTitle() + " to project " + project.getTitle());
        System.out.println("Task assigned successfully.");
    }

    private void editTask() {
        System.out.println("Which tasks would you like to edit? (Enter task number)");
        for(Task t : tasks) {
            System.out.println((tasks.indexOf(t) + 1) + ". " + t.getTitle());
        }
        int taskNumber = readInt("Enter task number: ");
        if (taskNumber >= 1 && taskNumber <= tasks.size()) {
            Task task = tasks.get(taskNumber - 1);
            System.out.println("What would you like to edit?");
            System.out.println("1. Title");
            System.out.println("2. Description");
            System.out.println("3. Priority Level");
            System.out.println("4. due date");
            System.out.println("5. status");
            System.out.println("6. associated project");
            System.out.println("7. tags");

            int editChoice = readInt("Enter your choice: ");
            switch (editChoice) {
                case 1:
                    System.out.print("Enter new task title: ");
                    String oldTitle = task.getTitle();
                    String newTitle = scanner.nextLine();
                    task.setTitle(newTitle);
                    logActivity("Updated task title from '" + oldTitle + "' to '" + newTitle + "'");
                    break;
                case 2:
                    System.out.print("Enter new task description: ");
                    String newDescription = scanner.nextLine();
                    task.setDescription(newDescription);
                    logActivity("Updated task description for '" + task.getTitle() + "'");
                    break;
                case 3:
                    System.out.print("Enter new task priority level (low, medium, high): ");
                    String oldPriority = task.getPriorityLevel();
                    String newPriority = scanner.nextLine();
                    task.setPriorityLevel(newPriority);
                    logActivity("Updated task priority from '" + oldPriority + "' to '" + newPriority + "' for '" + task.getTitle() + "'");
                    break;
                case 4:
                    System.out.print("Enter new task due date (YYYY-MM-DD): ");
                    String dueDateInput = scanner.nextLine();
                    try {
                        LocalDate oldDueDate = task.getDuedate();
                        LocalDate newDueDate = LocalDate.parse(dueDateInput);
                        task.setDuedate(newDueDate);
                        logActivity("Updated task due date from '" + oldDueDate + "' to '" + newDueDate + "' for '" + task.getTitle() + "'");
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Due date will not be updated.");
                    }
                    break;
                case 5:
                    System.out.print("Enter new task status (open / in progress / completed / cancelled): ");
                    String oldStatus = task.getStatus();
                    String newStatus = scanner.nextLine().trim();
                    task.setStatus(newStatus);
                    logActivity("Updated task status from '" + oldStatus + "' to '" + newStatus + "' for '" + task.getTitle() + "'");
                    if ("completed".equalsIgnoreCase(newStatus)) {
                        logActivity("Task completed: '" + task.getTitle() + "'");
                    } else if ("cancelled".equalsIgnoreCase(newStatus) || "canceled".equalsIgnoreCase(newStatus)) {
                        logActivity("Task cancelled: '" + task.getTitle() + "'");
                    }
                    break;
                case 6: {
                    Project currentProject = findProjectForTask(task);
                    System.out.println("Current project: " + (currentProject == null ? "None" : currentProject.getTitle()));
                    System.out.println("1. Remove from current project");
                    System.out.println("2. Move to a different project");
                    System.out.println("3. Cancel");
                    int projectEditChoice = readInt("Enter choice: ");
                    if (projectEditChoice == 1) {
                        if (currentProject != null) {
                            currentProject.removeTask(task);
                            logActivity("Removed task '" + task.getTitle() + "' from project '" + currentProject.getTitle() + "'");
                            System.out.println("Task removed from project.");
                        } else {
                            System.out.println("Task is not in any project.");
                        }
                    } else if (projectEditChoice == 2) {
                        if (projects.isEmpty()) {
                            System.out.println("No projects available.");
                        } else {
                            System.out.println("Select new project:");
                            for (int i = 0; i < projects.size(); i++) {
                                System.out.println((i + 1) + ". " + projects.get(i).getTitle());
                            }
                            int projectChoice = readInt("Enter project number: ");
                            if (projectChoice >= 1 && projectChoice <= projects.size()) {
                                Project newProject = projects.get(projectChoice - 1);
                                if (currentProject != null) {
                                    currentProject.removeTask(task);
                                }
                                newProject.addTask(task);
                                logActivity("Moved task '" + task.getTitle() + "' to project '" + newProject.getTitle() + "'");
                                System.out.println("Task moved to project '" + newProject.getTitle() + "'.");
                            } else {
                                System.out.println("Invalid project number.");
                            }
                        }
                    }
                    break;
                }
                case 7: {
                    System.out.println("Current tags: " + task.getTagsAsString());
                    System.out.println("1. Add tag");
                    System.out.println("2. Remove tag");
                    System.out.println("3. Clear all tags");
                    int tagEditChoice = readInt("Enter choice: ");
                    if (tagEditChoice == 1) {
                        System.out.print("Enter tag name to add: ");
                        String tagName = scanner.nextLine().trim();
                        if (!tagName.isEmpty()) {
                            task.addTag(new Tag(tagName));
                            logActivity("Added tag '" + tagName + "' to task '" + task.getTitle() + "'");
                            System.out.println("Tag added.");
                        }
                    } else if (tagEditChoice == 2) {
                        System.out.print("Enter tag name to remove: ");
                        String tagToRemove = scanner.nextLine().trim();
                        boolean tagRemoved = task.getTags().removeIf(t2 -> t2.getName().equalsIgnoreCase(tagToRemove));
                        if (tagRemoved) {
                            logActivity("Removed tag '" + tagToRemove + "' from task '" + task.getTitle() + "'");
                            System.out.println("Tag removed.");
                        } else {
                            System.out.println("Tag '" + tagToRemove + "' not found.");
                        }
                    } else if (tagEditChoice == 3) {
                        task.getTags().clear();
                        logActivity("Cleared all tags from task '" + task.getTitle() + "'");
                        System.out.println("All tags cleared.");
                    }
                    break;
                }
                default:
                    break;
            }
            try {
                TaskDAO.updateTask(task);
            } catch (Exception e) {
                System.out.println("Warning: could not persist change to database: " + e.getMessage());
            }

        } else {
            System.out.println("Invalid task number.");
        }
    }

    private void editProject() {
        System.out.println("Which projects would you like to edit? (Enter project number)");
        for(Project p : projects) {
            System.out.println((projects.indexOf(p) + 1) + ". " + p.getTitle());
        }
        int projectNumber = readInt("Enter project number: ");    
        if (projectNumber >= 1 && projectNumber <= projects.size()) {
            Project project = projects.get(projectNumber - 1);
            System.out.println("What would you like to edit?");
            System.out.println("1. Removing Task");
            System.out.println("2. Moving task to another project");
            System.out.println("3. Adding Tasks");
            System.out.println("4. Manage Collaborator Limits (US-17)");

            int editChoice = readInt("Enter your choice: ");
            switch (editChoice) {
                case 1:
                for(Task t : project.getTasks()) {
                    System.out.println((project.getTasks().indexOf(t) + 1) + ". " + t.getTitle());
                }
                int taskNumber = readInt("Enter task number to remove: ");
                if (taskNumber >= 1 && taskNumber <= project.getTasks().size()) {
                    Task taskToRemove = project.getTasks().get(taskNumber - 1);
                    project.removeTask(taskToRemove);
                    System.out.println("Task removed from project.");
                } else {
                    System.out.println("Invalid task number.");
                }
                    break;
                case 2:
                    System.out.print("Which task would you like to move? (Enter task number)");
                    for(Task t : project.getTasks()) {
                        System.out.println((project.getTasks().indexOf(t) + 1) + ". " + t.getTitle());
                    }
                    int taskNumberToMove = readInt("Enter task number to move: ");
                    if (taskNumberToMove >= 1 && taskNumberToMove <= project.getTasks().size()) {
                        Task taskToMove = project.getTasks().get(taskNumberToMove - 1);
                        System.out.println("Which project would you like to move the task to? (Enter project number)");
                        for(Project p : projects) {
                            if (p != project) {
                                System.out.println((projects.indexOf(p) + 1) + ". " + p.getTitle());
                            }
                        }
                        int targetProjectNumber = readInt("Enter target project number: ");
                        if (targetProjectNumber >= 1 && targetProjectNumber <= projects.size() && projects.get(targetProjectNumber - 1) != project) {
                            Project targetProject = projects.get(targetProjectNumber - 1);
                            project.removeTask(taskToMove);
                            targetProject.addTask(taskToMove);
                            System.out.println("Task moved to " + targetProject.getTitle());
                        } else {
                            System.out.println("Invalid project number.");
                        }
                    } else {
                        System.out.println("Invalid task number.");
                    }
                    break;
                case 3:
                    System.out.println("Which task would you like to add? (Enter task number)");
                    for(Task t : tasks) {
                        System.out.println((tasks.indexOf(t) + 1) + ". " + t.getTitle());
                    }
                    int taskNumberToAdd = readInt("Enter task number to add: ");
                    if (taskNumberToAdd >= 1 && taskNumberToAdd <= tasks.size()) {
                        Task taskToAdd = tasks.get(taskNumberToAdd - 1);
                        project.addTask(taskToAdd);
                        System.out.println("Task added to project.");
                    } else {
                        System.out.println("Invalid task number.");
                    }
                    break;
                case 4:
                    // US-17: Manage collaborator limits
                    manageCollaboratorLimits(project);
                    break;
                default:
                    break;
            }

        } else {
            System.out.println("Invalid project number.");
        }

    }

    private void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
        } else {
            System.out.println("How would you like to view tasks?");
            System.out.println("1. View by due date (soonest first)");
            System.out.println("2. Priority level (high to low)");
            System.out.println("3. Status (open, in progress, completed)");
            System.out.println("4. Project association");
            System.out.println("5. Tags");
            System.out.println("6. Specific date");
            System.out.println("7. Date range");
            int viewChoice = readInt("Enter your choice: ");
            switch (viewChoice) {
                case 1:
                    ArrayList<Task> sortedByDueDate = new ArrayList<>(tasks);
                    sortedByDueDate.sort(Comparator.comparing(Task::getDuedate,
                            Comparator.nullsLast(Comparator.naturalOrder())));
                    for (Task t : sortedByDueDate) {
                        System.out.println(t.toString());
                    }
                    break;
                case 2:
                    ArrayList<Task> sortedByPriority = new ArrayList<>(tasks);
                    sortedByPriority.sort(Comparator.comparingInt(task -> {
                        String level = task.getPriorityLevel();
                        if (level == null) return Integer.MAX_VALUE;
                        switch (level.toLowerCase()) {
                            case "high":
                                return 0;
                            case "medium":
                                return 1;
                            case "low":
                                return 2;
                            default:
                                return 3;
                        }
                    }));
                    for (Task t : sortedByPriority) {
                        System.out.println(t.toString());
                    }
                    break;
                case 3:
                    for (Task t : tasks) {
                        if ("open".equalsIgnoreCase(t.getStatus())) {
                            System.out.println(t.toString());
                        }
                    }
                    break;
                case 4:
                    for (Project p : projects) {
                        System.out.println("Project: " + p.getTitle());
                        for (Task t : p.getTasks()) {
                            System.out.println(t.toString());
                        }
                    }
                    break;
                case 5:
                    ArrayList<Task> sortedByTags = new ArrayList<>(tasks);
                    sortedByTags.sort(Comparator.comparing(Task::getTagsAsString,
                            Comparator.nullsLast(Comparator.naturalOrder())));
                    for (Task t : sortedByTags) {
                        System.out.println("Task: " + t.getTitle());
                        System.out.println("Tags: " + t.getTagsAsString());
                    }
                    break;
                case 6:
                    System.out.print("Enter the date to filter by (YYYY-MM-DD): ");
                    String specificDateInput = scanner.nextLine();
                    try {
                        LocalDate specificDate = LocalDate.parse(specificDateInput);
                        boolean found = false;
                        for (Task t : tasks) {
                            if (specificDate.equals(t.getDuedate())) {
                                System.out.println(t.toString());
                                found = true;
                            }
                        }
                        if (!found) {
                            System.out.println("No tasks found for " + specificDate + ".");
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                    }
                    break;
                case 7:
                    System.out.print("Enter start date (YYYY-MM-DD): ");
                    String startDateInput = scanner.nextLine();
                    System.out.print("Enter end date (YYYY-MM-DD): ");
                    String endDateInput = scanner.nextLine();
                    try {
                        LocalDate startDate = LocalDate.parse(startDateInput);
                        LocalDate endDate = LocalDate.parse(endDateInput);
                        if (endDate.isBefore(startDate)) {
                            System.out.println("End date must be the same or after the start date.");
                        } else {
                            boolean foundRange = false;
                            for (Task t : tasks) {
                                LocalDate due = t.getDuedate();
                                if (due != null && !due.isBefore(startDate) && !due.isAfter(endDate)) {
                                    System.out.println(t.toString());
                                    foundRange = true;
                                }
                            }
                            if (!foundRange) {
                                System.out.println("No tasks found in this date range.");
                            }
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Displaying all tasks without sorting.");
            }

        }
    }

    private void searchTasks() {
        System.out.println("Search criteria (leave blank to skip):");
        System.out.print("Keyword (matches task title or description): ");
        String keyword = scanner.nextLine().trim().toLowerCase();
        System.out.print("Status (open, in progress, completed): ");
        String statusCriteria = scanner.nextLine().trim().toLowerCase();
        System.out.print("Start due date (YYYY-MM-DD): ");
        String startDateInput = scanner.nextLine().trim();
        System.out.print("End due date (YYYY-MM-DD): ");
        String endDateInput = scanner.nextLine().trim();
        System.out.print("Day of week (MONDAY...SUNDAY): ");
        String dayOfWeek = scanner.nextLine().trim().toUpperCase();

        LocalDate startDate = null;
        LocalDate endDate = null;
        try {
            if (!startDateInput.isEmpty()) {
                startDate = LocalDate.parse(startDateInput);
            }
            if (!endDateInput.isEmpty()) {
                endDate = LocalDate.parse(endDateInput);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date; ignoring date filter.");
        }

        boolean noCriteria = keyword.isEmpty() && statusCriteria.isEmpty()
                && startDate == null && endDate == null && dayOfWeek.isEmpty();

        List<Task> matches = new ArrayList<>();
        for (Task t : tasks) {
            if (noCriteria && !"open".equalsIgnoreCase(t.getStatus())) {
                continue;
            }
            if (!keyword.isEmpty()) {
                String title = t.getTitle() == null ? "" : t.getTitle().toLowerCase();
                String description = t.getDescription() == null ? "" : t.getDescription().toLowerCase();
                if (!title.contains(keyword) && !description.contains(keyword)) {
                    continue;
                }
            }
            if (!statusCriteria.isEmpty() && (t.getStatus() == null || !t.getStatus().equalsIgnoreCase(statusCriteria))) {
                continue;
            }
            if (startDate != null && (t.getDuedate() == null || t.getDuedate().isBefore(startDate))) {
                continue;
            }
            if (endDate != null && (t.getDuedate() == null || t.getDuedate().isAfter(endDate))) {
                continue;
            }
            if (!dayOfWeek.isEmpty() && (t.getDuedate() == null || !t.getDuedate().getDayOfWeek().name().equalsIgnoreCase(dayOfWeek))) {
                continue;
            }
            matches.add(t);
        }

        if (matches.isEmpty()) {
            System.out.println("No tasks found for criteria.");
        } else {
            matches.sort(Comparator.comparing(Task::getDuedate, Comparator.nullsLast(Comparator.naturalOrder())));
            for (Task t : matches) {
                System.out.println(t.toString());
            }
            logActivity("Performed search with criteria; result size=" + matches.size());
            System.out.print("Export this search result to CSV? (y/n): ");
            String exportChoice = scanner.nextLine().trim().toLowerCase();
            if (exportChoice.startsWith("y")) {
                exportTasksToCSV(matches);
            }
        }
    }

    private void viewTaskHistory() {
        if (activityHistory.isEmpty()) {
            System.out.println("No task activity history available.");
            return;
        }
        System.out.println("Task activity history:");
        for (ActivityEntry entry : activityHistory) {
            System.out.println(entry.toString());
        }
    }

    private void logActivity(String description) {
        ActivityEntry entry = new ActivityEntry(LocalDateTime.now(), description);
        activityHistory.add(entry);
    }

    private boolean isTaskNameDueDateUnique(String title, LocalDate dueDate) {
        for (Task t : tasks) {
            if (title != null && title.equalsIgnoreCase(t.getTitle()) && dueDate != null && dueDate.equals(t.getDuedate())) {
                return false;
            }
        }
        return true;
    }

    private Project getOrCreateProject(String projectName, String projectDescription) {
        if (projectName == null) return null;
        for (Project p : projects) {
            if (projectName.equalsIgnoreCase(p.getTitle())) {
                return p;
            }
        }
        Project newProject = new Project(projectName, projectDescription);
        projects.add(newProject);
        logActivity("Created project: '" + projectName + "'");
        return newProject;
    }

    private int getOpenTasksCountForCollaborator(String collaboratorName) {
        if (collaboratorName == null) return 0;
        int count = 0;
        for (Task task : tasks) {
            if (task.hasCollaborator(collaboratorName)
                    && !"completed".equalsIgnoreCase(task.getStatus())
                    && !"cancelled".equalsIgnoreCase(task.getStatus())
                    && !"canceled".equalsIgnoreCase(task.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private void exportTasksToCSV(List<Task> tasksToExport) {
        System.out.print("Enter output CSV file path (e.g., tasks.csv): ");
        String filePath = scanner.nextLine().trim();
        if (filePath.isEmpty()) {
            filePath = "tasks.csv";
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Task name,description,subtask,status,priority,due date,projectName,projectDescription,collaborator,collaborator category\n");

            for (Task task : tasksToExport) {
                String subtaskNames = "";
                if (task.getSubtask() != null && !task.getSubtask().isEmpty()) {
                    StringBuilder subtaskSb = new StringBuilder();
                    for (Subtask s : task.getSubtask()) {
                        if (subtaskSb.length() > 0) subtaskSb.append(";");
                        subtaskSb.append(s.getTitle());
                    }
                    subtaskNames = subtaskSb.toString();
                }

                String projectName = "";
                String projectDescription = "";
                Project projectForTask = findProjectForTask(task);
                if (projectForTask != null) {
                    projectName = projectForTask.getTitle() == null ? "" : projectForTask.getTitle();
                    projectDescription = projectForTask.getDescription() == null ? "" : projectForTask.getDescription();
                }

                String collaboratorName = "";
                String collaboratorCategory = "";
                if (task.getCollaborator() != null) {
                    collaboratorName = task.getCollaborator();
                }
                if (task.getCollaboratorCategory() != null) {
                    collaboratorCategory = task.getCollaboratorCategory();
                }

                writer.write(csvEscape(task.getTitle()) + ","
                        + csvEscape(task.getDescription()) + ","
                        + csvEscape(subtaskNames) + ","
                        + csvEscape(task.getStatus()) + ","
                        + csvEscape(task.getPriorityLevel()) + ","
                        + csvEscape(task.getDuedate() == null ? "" : task.getDuedate().toString()) + ","
                        + csvEscape(projectName) + ","
                        + csvEscape(projectDescription) + ","
                        + csvEscape(collaboratorName) + ","
                        + csvEscape(collaboratorCategory) + "\n");
            }

            System.out.println("Search results exported to " + filePath);
            logActivity("Exported " + tasksToExport.size() + " task(s) to CSV: " + filePath);
        } catch (IOException e) {
            System.out.println("Failed to write CSV file: " + e.getMessage());
        }
    }

    private void exportSingleTaskToICal() {
        List<Task> exportableTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getDuedate() != null) {
                exportableTasks.add(task);
            }
        }

        if (exportableTasks.isEmpty()) {
            System.out.println("No tasks with due dates are available for iCal export.");
            return;
        }

        exportableTasks.sort(Comparator.comparing(Task::getDuedate));
        System.out.println("Tasks available for iCal export:");
        for (int i = 0; i < exportableTasks.size(); i++) {
            Task task = exportableTasks.get(i);
            Project project = findProjectForTask(task);
            String projectName = project == null ? "No project" : project.getTitle();
            System.out.println((i + 1) + ". " + task.getTitle() + " | Due: " + task.getDuedate() + " | Project: " + projectName);
        }

        int selection = readInt("Select task number to export: ");
        if (selection < 1 || selection > exportableTasks.size()) {
            System.out.println("Invalid task number.");
            return;
        }

        Task selectedTask = exportableTasks.get(selection - 1);
        Project project = findProjectForTask(selectedTask);
        String defaultName = (selectedTask.getTitle() == null || selectedTask.getTitle().trim().isEmpty())
                ? "task.ics"
                : selectedTask.getTitle().trim().replaceAll("[^a-zA-Z0-9-_]+", "_") + ".ics";
        System.out.print("Enter output .ics file path (leave blank for " + defaultName + "): ");
        String outputPathInput = scanner.nextLine().trim();

        try {
            String exportedPath = taskExportGateway.exportTask(selectedTask, project, outputPathInput);
            String absolutePath = Paths.get(exportedPath).toAbsolutePath().toString();
            System.out.println("Task exported successfully to: " + absolutePath);
            logActivity("Exported task '" + selectedTask.getTitle() + "' to iCal file: " + absolutePath);
        } catch (IllegalArgumentException e) {
            System.out.println("Task export failed: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to write iCal file: " + e.getMessage());
        }
    }

    private void exportProjectTasksToICal() {
        if (projects.isEmpty()) {
            System.out.println("No projects available for export.");
            return;
        }

        System.out.println("Projects available for iCal export:");
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            System.out.println((i + 1) + ". " + project.getTitle());
        }

        int selection = readInt("Select project number to export: ");
        if (selection < 1 || selection > projects.size()) {
            System.out.println("Invalid project number.");
            return;
        }

        Project selectedProject = projects.get(selection - 1);
        int exportedCount = 0;
        int skippedCount = 0;

        for (Task task : selectedProject.getTasks()) {
            if (task == null || task.getDuedate() == null) {
                skippedCount++;
            } else {
                exportedCount++;
            }
        }

        if (exportedCount == 0) {
            System.out.println("No due-dated tasks found in this project. Export skipped. Skipped tasks: " + skippedCount);
            return;
        }

        String defaultName = (selectedProject.getTitle() == null || selectedProject.getTitle().trim().isEmpty())
                ? "project_tasks.ics"
                : selectedProject.getTitle().trim().replaceAll("[^a-zA-Z0-9-_]+", "_") + "_tasks.ics";
        System.out.print("Enter output .ics file path (leave blank for " + defaultName + "): ");
        String outputPathInput = scanner.nextLine().trim();

        try {
            String exportedPath = taskExportGateway.exportProjectTasks(selectedProject, outputPathInput);
            String absolutePath = Paths.get(exportedPath).toAbsolutePath().toString();
            System.out.println("Project export successful: " + absolutePath);
            System.out.println("Tasks exported: " + exportedCount + " | Tasks skipped (no due date): " + skippedCount);
            logActivity("Exported project '" + selectedProject.getTitle() + "' to iCal file: " + absolutePath
                    + " (exported=" + exportedCount + ", skipped=" + skippedCount + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("Project export failed: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to write iCal file: " + e.getMessage());
        }
    }

    private void exportFilteredTasksToICal() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available to export.");
            return;
        }

        System.out.println("--- Filter Criteria (leave blank to skip) ---");
        System.out.print("Status (open / in progress / completed): ");
        String statusFilter = scanner.nextLine().trim().toLowerCase();

        System.out.print("Priority (low / medium / high): ");
        String priorityFilter = scanner.nextLine().trim().toLowerCase();

        LocalDate startDate = null;
        LocalDate endDate = null;
        System.out.print("Due date from (YYYY-MM-DD): ");
        String startInput = scanner.nextLine().trim();
        System.out.print("Due date to   (YYYY-MM-DD): ");
        String endInput = scanner.nextLine().trim();
        try {
            if (!startInput.isEmpty()) startDate = LocalDate.parse(startInput);
            if (!endInput.isEmpty())  endDate   = LocalDate.parse(endInput);
        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("Invalid date format; date filter will be ignored.");
        }

        List<Task> filtered = new ArrayList<>();
        Map<Task, Project> taskToProject = new HashMap<>();

        for (Task task : tasks) {
            if (task.getDuedate() == null) continue;
            if (!statusFilter.isEmpty() && !statusFilter.equalsIgnoreCase(task.getStatus())) continue;
            if (!priorityFilter.isEmpty() && !priorityFilter.equalsIgnoreCase(task.getPriorityLevel())) continue;
            if (startDate != null && task.getDuedate().isBefore(startDate)) continue;
            if (endDate   != null && task.getDuedate().isAfter(endDate))   continue;
            filtered.add(task);
            taskToProject.put(task, findProjectForTask(task));
        }

        if (filtered.isEmpty()) {
            System.out.println("No tasks match the filter criteria (or none have due dates). Export cancelled.");
            return;
        }

        System.out.println(filtered.size() + " task(s) match the filter:");
        for (Task t : filtered) {
            Project p = taskToProject.get(t);
            String projectName = p == null ? "No project" : p.getTitle();
            System.out.println("  - " + t.getTitle() + " | Due: " + t.getDuedate()
                    + " | Status: " + t.getStatus() + " | Priority: " + t.getPriorityLevel()
                    + " | Project: " + projectName);
        }

        System.out.print("Enter output .ics file path (leave blank for filtered_tasks.ics): ");
        String outputPath = scanner.nextLine().trim();

        try {
            String exportedPath = taskExportGateway.exportFilteredTasks(filtered, taskToProject, outputPath);
            String absolutePath = Paths.get(exportedPath).toAbsolutePath().toString();
            System.out.println(filtered.size() + " task(s) exported successfully to: " + absolutePath);
            logActivity("Exported " + filtered.size() + " filtered task(s) to iCal: " + absolutePath);
        } catch (IllegalArgumentException e) {
            System.out.println("Export failed: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to write iCal file: " + e.getMessage());
        }
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"") ) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private Project findProjectForTask(Task task) {
        for (Project project : projects) {
            if (project.getTasks().contains(task)) {
                return project;
            }
        }
        return null;
    }

    private void importTasksFromCSV() {
        System.out.print("Enter CSV file path to import: ");
        String filePath = scanner.nextLine().trim();
        if (filePath.isEmpty()) {
            System.out.println("CSV file path cannot be empty.");
            return;
        }

        int addedCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine();
            if (header == null) {
                System.out.println("CSV is empty.");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = parseCsvLine(line);
                if (columns.length < 10) {
                    System.out.println("Skipping invalid row (not enough columns): " + line);
                    continue;
                }

                Task task = new Task();
                task.setTitle(columns[0].trim());
                task.setDescription(columns[1].trim());
                String subtaskInfo = columns[2].trim();
                if (!subtaskInfo.isEmpty()) {
                    String[] subtaskNames = subtaskInfo.split(";");
                    for (String subtaskName : subtaskNames) {
                        subtaskName = subtaskName.trim();
                        if (!subtaskName.isEmpty()) {
                            try {
                                task.addSubtask(new Subtask(subtaskName, ""));
                            } catch (SubtaskLimitExceededException e) {
                                System.out.println("Warning: " + e.getMessage() + " Skipping remaining subtasks for this task.");
                                break;
                            }
                        }
                    }
                }
                task.setStatus(columns[3].trim());
                task.setPriorityLevel(columns[4].trim());
                String dueDateString = columns[5].trim();
                if (!dueDateString.isEmpty()) {
                    try {
                        task.setDuedate(LocalDate.parse(dueDateString));
                    } catch (DateTimeParseException e) {
                        System.out.println("Warning: invalid due date on row, skipping due date: " + dueDateString);
                    }
                }

                String projectName = columns[6].trim();
                String projectDescription = columns[7].trim();
                Project project = null;
                if (!projectName.isEmpty()) {
                    for (Project p : projects) {
                        if (projectName.equalsIgnoreCase(p.getTitle())) {
                            project = p;
                            break;
                        }
                    }
                    if (project == null) {
                        project = new Project(projectName, projectDescription);
                        projects.add(project);
                    }
                }

                String collaboratorName = columns[8].trim();
                String collaboratorCategory = columns.length > 9 ? columns[9].trim() : "";
                if (!collaboratorName.isEmpty() && !isSelfAssignmentName(collaboratorName)) {
                    if (project == null) {
                        System.out.println("Skipping row: collaborator assignments require a project. Row task=" + task.getTitle());
                        continue;
                    }

                    Collaborator coll = project.getCollaboratorByName(collaboratorName);
                    if (coll == null) {
                        String normalizedCategory = normalizeCollaboratorCategory(collaboratorCategory);
                        if (normalizedCategory == null) {
                            System.out.println("Skipping row: invalid collaborator category for " + collaboratorName + ": " + collaboratorCategory);
                            continue;
                        }
                        coll = new Collaborator(collaboratorName, normalizedCategory);
                        project.addCollaborator(coll);
                    }

                    int openCount = getOpenTasksCountForCollaborator(collaboratorName);
                    if (openCount >= coll.getOpenTaskLimit()) {
                        System.out.println("Collaborator " + collaboratorName + " is at limit; skipping this task.");
                        continue;
                    }

                    task.addCollaboratorAssignment(collaboratorName, coll.getCategory());
                    try {
                        task.addSubtask(new Subtask("Collaborator: " + collaboratorName, "Assigned collaborator task"));
                    } catch (SubtaskLimitExceededException e) {
                        System.out.println("Warning: " + e.getMessage() + " Skipping collaborator linkage for this task.");
                    }
                }

                if (!isTaskNameDueDateUnique(task.getTitle(), task.getDuedate())) {
                    System.out.println("Skipping duplicate task with same title and due date: " + task.getTitle() + " " + task.getDuedate());
                    continue;
                }

                tasks.add(task);
                addedCount++;
                try {
                    TaskDAO.insertTask(task);
                } catch (Exception e) {
                    System.out.println("Warning: could not save task to database: " + e.getMessage());
                }

                if (project != null) {
                    project.addTask(task);
                }
            }

            logActivity("Imported " + addedCount + " task(s) from CSV: " + filePath);
            System.out.println("Imported " + addedCount + " task(s) from CSV.");
        } catch (IOException e) {
            System.out.println("Failed to read CSV file: " + e.getMessage());
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }

    private boolean isSelfAssignmentName(String collaboratorName) {
        if (collaboratorName == null) {
            return false;
        }
        String normalized = collaboratorName.trim().toLowerCase();
        return "self".equals(normalized) || "me".equals(normalized) || "current user".equals(normalized);
    }

    private boolean linkExternalCollaboratorToTask(Task task, Project project, String collaboratorName) {
        if (task == null || project == null || collaboratorName == null || collaboratorName.trim().isEmpty()) {
            return false;
        }

        Collaborator collaborator = project.getCollaboratorByName(collaboratorName);
        if (collaborator == null) {
            System.out.print("Collaborator category for " + collaboratorName + " (Junior/Intermediate/Senior): ");
            String category = scanner.nextLine().trim();
            String normalizedCategory = normalizeCollaboratorCategory(category);
            if (normalizedCategory == null) {
                System.out.println("Invalid collaborator category. Use Junior, Intermediate, or Senior.");
                return false;
            }
            collaborator = new Collaborator(collaboratorName, normalizedCategory);
            project.addCollaborator(collaborator);
        }

        if (task.hasCollaborator(collaboratorName)) {
            return true;
        }

        int openTasks = getOpenTasksCountForCollaborator(collaboratorName);
        int limit = collaborator.getOpenTaskLimit();
        if (openTasks >= limit) {
            System.out.println("Collaborator " + collaboratorName + " has reached limit (" + limit + ") of open tasks.");
            return false;
        }

        task.addCollaboratorAssignment(collaboratorName, collaborator.getCategory());
        Subtask collSubtask = new Subtask("Collaborator: " + collaboratorName, "Assigned collaborator task");
        try {
            task.addSubtask(collSubtask);
        } catch (SubtaskLimitExceededException e) {
            System.out.println("ERROR: " + e.getMessage());
            return false;
        }

        logActivity("Assigned collaborator " + collaboratorName + " to task " + task.getTitle());
        return true;
    }

    private String normalizeCollaboratorCategory(String categoryInput) {
        if (categoryInput == null) {
            return null;
        }
        switch (categoryInput.trim().toLowerCase()) {
            case "junior":
                return "Junior";
            case "intermediate":
                return "Intermediate";
            case "senior":
                return "Senior";
            default:
                return null;
        }
    }

    private void viewOverloadedCollaborators() {
        System.out.println("=============================");
        System.out.println("   Overloaded Collaborators  ");
        System.out.println("=============================");
        List<Collaborator> allCollaborators = new ArrayList<>();
        for (Project project : projects) {
            for (Collaborator c : project.getCollaborators()) {
                boolean exists = false;
                for (Collaborator existing : allCollaborators) {
                    if (existing.getName().equalsIgnoreCase(c.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    allCollaborators.add(c);
                }
            }
        }
        if (allCollaborators.isEmpty()) {
            System.out.println("No collaborators found in any project.");
            return;
        }
        List<Collaborator> overloaded = collaboratorService.getOverloadedCollaborators(allCollaborators);
        if (overloaded.isEmpty()) {
            System.out.println("No overloaded collaborators. All workloads are balanced.");
        } else {
            System.out.println("The following collaborators are overloaded:");
            for (Collaborator c : overloaded) {
                int openTasks = collaboratorService.getOpenTaskCount(c.getName());
                int limit = c.getOpenTaskLimit();
                System.out.println("  - " + c.getName() + " (" + c.getCategory() + "): " + openTasks + "/" + limit + " tasks");
            }
        }
        System.out.println("-----------------------------");
        System.out.println("Collaborator Workload Summary:");
        for (Collaborator c : allCollaborators) {
            System.out.println(collaboratorService.getWorkloadStatus(c));
        }
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    /**
     * US-16: Count open tasks without a due date
     * These tasks count toward the 50-task limit
     */
    private int countOpenTasksWithoutDueDate() {
        int count = 0;
        for (Task task : tasks) {
            if ("open".equalsIgnoreCase(task.getStatus()) && task.getDuedate() == null) {
                count++;
            }
        }
        return count;
    }

    /**
     * US-16: Add a due date to an existing open task
     * This reduces the count of tasks subject to the limit
     */
    public void addDueDateToTask(Task task, LocalDate dueDate) {
        if (task != null && dueDate != null) {
            task.setDuedate(dueDate);
        }
    }

    /**
     * US-17: Manage collaborator category limits for a project
     * Allows setting positive integer limits only
     */
    private void manageCollaboratorLimits(Project project) {
        if (project.getCollaborators().isEmpty()) {
            System.out.println("No collaborators in this project.");
            return;
        }

        System.out.println("Project collaborators:");
        for (int i = 0; i < project.getCollaborators().size(); i++) {
            Collaborator c = project.getCollaborators().get(i);
            System.out.println((i + 1) + ". " + c.getName() + " (" + c.getCategory() + ") - Current limit: " + c.getOpenTaskLimit());
        }

        int collabNumber = readInt("Enter collaborator number to set limit: ");
        if (collabNumber >= 1 && collabNumber <= project.getCollaborators().size()) {
            Collaborator collaborator = project.getCollaborators().get(collabNumber - 1);
            System.out.print("Enter new open task limit (must be positive integer): ");
            String limitInput = scanner.nextLine().trim();
            try {
                setCollaboratorCategoryLimit(collaborator, limitInput);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Invalid collaborator number.");
        }
    }

    /**
     * US-17: Validate and set collaborator category limit
     * Only accepts positive integers
     */
    public void setCollaboratorCategoryLimit(Collaborator collaborator, String limitInput) throws IllegalArgumentException {
        try {
            int limit = Integer.parseInt(limitInput);
            if (limit <= 0) {
                throw new IllegalArgumentException("ERROR: Collaborator category limit must be a positive integer. Got: " + limit);
            }
            collaborator.setCustomOpenTaskLimit(limit);
            System.out.println("Collaborator limit set successfully to: " + limit);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ERROR: Invalid input. Collaborator category limit must be a positive integer, not: " + limitInput);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Task t : tasks) {
            sb.append("Title: ").append(t.getTitle()).append("\n");
            sb.append("Description: ").append(t.getDescription()).append("\n");
            sb.append("Priority: ").append(t.getPriorityLevel()).append("\n");
            sb.append("Due Date: ").append(t.getDuedate()).append("\n");
            sb.append("Status: ").append(t.getStatus()).append("\n");
            sb.append("-----------------------------\n");
        }
        return sb.toString();
    }
}
