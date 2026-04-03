package com.example.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.example.CollaboratorService;
import com.example.TaskExportGateway;
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

        // US-16: Check if we can create an open task without a due date
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

        System.out.print("Assign collaborator (name) (optional): ");
        String collaboratorName = scanner.nextLine().trim();
        if (!collaboratorName.isEmpty()) {
            System.out.print("Collaborator category (Junior/Intermediate/Senior): ");
            String collaboratorCategory = scanner.nextLine().trim();
            newTask.setCollaborator(collaboratorName);
            newTask.setCollaboratorCategory(collaboratorCategory);

            if (project != null) {
                Collaborator existingColl = project.getCollaboratorByName(collaboratorName);
                if (existingColl == null) {
                    existingColl = new Collaborator(collaboratorName, collaboratorCategory);
                    project.addCollaborator(existingColl);
                }

                int openTasks = getOpenTasksCountForCollaborator(collaboratorName);
                int limit = existingColl.getOpenTaskLimit();
                if (openTasks >= limit) {
                    System.out.println("Collaborator " + collaboratorName + " has reached limit (" + limit + ") of open tasks.");
                    return;
                }

                // create the linked subtask for collaborator progress
                Subtask collSubtask = new Subtask("Collaborator: " + collaboratorName, "Assigned collaborator task");
                try {
                    newTask.addSubtask(collSubtask);
                } catch (SubtaskLimitExceededException e) {
                    System.out.println("ERROR: " + e.getMessage());
                    return;
                }

            }
        }

        System.out.print("Recurrence type (none/daily/weekly/monthly): ");
        String recurrenceType = scanner.nextLine().trim().toLowerCase();
        if (recurrenceType.isEmpty()) {
            recurrenceType = "none";
        }
        newTask.setRecurrenceType(recurrenceType);

        if ("none".equalsIgnoreCase(recurrenceType)) {
            newTask.setRecurrenceStart(dueDate);
            newTask.setRecurrenceEnd(dueDate);
        } else {
            newTask.setRecurrenceStart(dueDate);
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
            try {
                LocalDate endDate = LocalDate.parse(endDateInput);
                newTask.setRecurrenceEnd(endDate);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid end date; using start date as end date.");
                newTask.setRecurrenceEnd(dueDate);
            }

            if ("weekly".equalsIgnoreCase(recurrenceType)) {
                System.out.print("Enter weekdays for recurrence (comma-separated MONDAY,TUESDAY...): ");
                String daysInput = scanner.nextLine().trim();
                ArrayList<String> weekdays = new ArrayList<>();
                for (String day : daysInput.split(",")) {
                    if (!day.trim().isEmpty()) {
                        weekdays.add(day.trim().toUpperCase());
                    }
                }
                if (weekdays.isEmpty()) {
                    weekdays.add(dueDate.getDayOfWeek().name());
                }
                newTask.setRecurrenceWeekdays(weekdays);
            }

        }

        if ("none".equalsIgnoreCase(newTask.getRecurrenceType())) {
            tasks.add(newTask);
            if (project != null) project.addTask(newTask);
            logActivity("Created task: '" + newTask.getTitle() + "' with due date " + newTask.getDuedate());
        } else {
            List<Task> occurrences = generateRecurringTasks(newTask);
            for (Task occurrence : occurrences) {
                if (occurrence.getDuedate() != null && isTaskNameDueDateUnique(occurrence.getTitle(), occurrence.getDuedate())) {
                    tasks.add(occurrence);
                    if (project != null) project.addTask(occurrence);
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
        LocalDate current = baseTask.getRecurrenceStart();
        LocalDate end = baseTask.getRecurrenceEnd();
        String type = baseTask.getRecurrenceType() == null ? "none" : baseTask.getRecurrenceType().toLowerCase();
        int interval = Math.max(1, baseTask.getRecurrenceInterval());

        while (!current.isAfter(end)) {
            Task occurrence = new Task();
            occurrence.setTitle(baseTask.getTitle());
            occurrence.setDescription(baseTask.getDescription());
            occurrence.setPriorityLevel(baseTask.getPriorityLevel());
            occurrence.setStatus(baseTask.getStatus());
            occurrence.setTags(baseTask.getTags());
            occurrence.setCollaborator(baseTask.getCollaborator());
            occurrence.setCollaboratorCategory(baseTask.getCollaboratorCategory());
            occurrence.setRecurrenceType(baseTask.getRecurrenceType());
            occurrence.setRecurrenceInterval(baseTask.getRecurrenceInterval());
            occurrence.setRecurrenceWeekdays(baseTask.getRecurrenceWeekdays());
            occurrence.setRecurrenceStart(baseTask.getRecurrenceStart());
            occurrence.setRecurrenceEnd(baseTask.getRecurrenceEnd());
            occurrence.setDuedate(current);
            occurrence.setSubtask(baseTask.getSubtask());
            occurrences.add(occurrence);

            switch (type) {
                case "daily":
                    current = current.plusDays(interval);
                    break;
                case "weekly":
                    if (baseTask.getRecurrenceWeekdays() == null || baseTask.getRecurrenceWeekdays().isEmpty()) {
                        current = current.plusWeeks(interval);
                    } else {
                        // Next matching weekday
                        LocalDate next = current.plusDays(1);
                        while (!next.isAfter(end)) {
                            if (baseTask.getRecurrenceWeekdays().contains(next.getDayOfWeek().name())) {
                                current = next;
                                break;
                            }
                            next = next.plusDays(1);
                        }
                        if (next.isAfter(end)) {
                            current = end.plusDays(1);
                        }
                    }
                    break;
                case "monthly":
                    current = current.plusMonths(interval);
                    break;
                default:
                    current = end.plusDays(1);
                    break;
            }
        }
        return occurrences;
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

        System.out.print("Assign collaborator name (optional): ");
        String collaboratorName = scanner.nextLine().trim();
        if (!collaboratorName.isEmpty()) {
            Collaborator collaborator = project.getCollaboratorByName(collaboratorName);
            if (collaborator == null) {
                System.out.print("Collaborator category (Junior/Intermediate/Senior): ");
                String category = scanner.nextLine().trim();
                collaborator = new Collaborator(collaboratorName, category);
                project.addCollaborator(collaborator);
            }
            int openTasks = getOpenTasksCountForCollaborator(collaboratorName);
            if (openTasks >= collaborator.getOpenTaskLimit()) {
                System.out.println("Collaborator " + collaboratorName + " has reached the open task limit.");
                return;
            }
            selectedTask.setCollaborator(collaboratorName);
            selectedTask.setCollaboratorCategory(collaborator.getCategory());

            Subtask collSubtask = new Subtask("Collaborator task: " + collaboratorName, "Assigned collaborator");
            try {
                selectedTask.addSubtask(collSubtask);
            } catch (SubtaskLimitExceededException e) {
                System.out.println("ERROR: " + e.getMessage());
                return;
            }
            logActivity("Assigned collaborator " + collaboratorName + " to task " + selectedTask.getTitle());
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
                    System.out.print("Enter new task status (open, in progress, completed): ");
                    String oldStatus = task.getStatus();
                    String newStatus = scanner.nextLine();
                    task.setStatus(newStatus);
                    logActivity("Updated task status from '" + oldStatus + "' to '" + newStatus + "' for '" + task.getTitle() + "'");
                    if ("completed".equalsIgnoreCase(newStatus)) {
                        logActivity("Task completed: '" + task.getTitle() + "'");
                    } else if ("cancelled".equalsIgnoreCase(newStatus) || "canceled".equalsIgnoreCase(newStatus)) {
                        logActivity("Task cancelled: '" + task.getTitle() + "'");
                    }
                    break;  
                case 6:
                    System.out.println("[Edit associated project] - functionality to be implemented.");
                    break;
                case 7:
                    System.out.println("[Edit tags] - functionality to be implemented.");
                    break;  
            
                default:
                    break;
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
                        if(t.getStatus() == "open") {
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
        System.out.print("Task name contains: ");
        String nameMatch = scanner.nextLine().trim().toLowerCase();
        System.out.print("Task description contains: ");
        String descriptionMatch = scanner.nextLine().trim().toLowerCase();
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

        boolean noCriteria = nameMatch.isEmpty() && descriptionMatch.isEmpty() && statusCriteria.isEmpty()
                && startDate == null && endDate == null && dayOfWeek.isEmpty();

        List<Task> matches = new ArrayList<>();
        for (Task t : tasks) {
            if (noCriteria && !"open".equalsIgnoreCase(t.getStatus())) {
                continue;
            }
            if (!nameMatch.isEmpty() && (t.getTitle() == null || !t.getTitle().toLowerCase().contains(nameMatch))) {
                continue;
            }
            if (!descriptionMatch.isEmpty() && (t.getDescription() == null || !t.getDescription().toLowerCase().contains(descriptionMatch))) {
                continue;
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
            if (collaboratorName.equalsIgnoreCase(task.getCollaborator()) && !"completed".equalsIgnoreCase(task.getStatus()) ) {
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

                String collaboratorName = columns[8].trim();
                String collaboratorCategory = columns.length > 9 ? columns[9].trim() : "";
                if (!collaboratorName.isEmpty()) {
                    task.setCollaborator(collaboratorName);
                    task.setCollaboratorCategory(collaboratorCategory);
                }

                if (!isTaskNameDueDateUnique(task.getTitle(), task.getDuedate())) {
                    System.out.println("Skipping duplicate task with same title and due date: " + task.getTitle() + " " + task.getDuedate());
                    continue;
                }

                tasks.add(task);
                addedCount++;

                String projectName = columns[6].trim();
                String projectDescription = columns[7].trim();
                if (!projectName.isEmpty()) {
                    Project project = null;
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

                    String collaboratorNameRow = columns[8].trim();
                    String collaboratorCategoryRow = columns.length > 9 ? columns[9].trim() : "";
                    if (!collaboratorNameRow.isEmpty()) {
                        Collaborator coll = project.getCollaboratorByName(collaboratorNameRow);
                        if (coll == null) {
                            coll = new Collaborator(collaboratorNameRow, collaboratorCategoryRow);
                            project.addCollaborator(coll);
                        }
                        int openCount = getOpenTasksCountForCollaborator(collaboratorNameRow);
                        if (openCount >= coll.getOpenTaskLimit()) {
                            System.out.println("Collaborator " + collaboratorNameRow + " is at limit; skipping this task.");
                            continue;
                        }
                    }

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
