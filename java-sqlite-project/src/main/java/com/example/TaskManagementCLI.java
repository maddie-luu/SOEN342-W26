package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Simple command-line interface for task and project management.
 * This is the first step: providing the user menu and
 * wiring up placeholders for the required actions.
 */
public class TaskManagementCLI {

    private final Scanner scanner;
    public final ArrayList<Task> tasks = new ArrayList<>();    
    public final ArrayList<Project> projects = new ArrayList<>();
    private final ArrayList<String> activityHistory = new ArrayList<>();

    public TaskManagementCLI() {
        this.scanner = new Scanner(System.in);
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
        System.out.println("10. Exit");
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
        newTask.setTitle(scanner.nextLine());
        System.out.print("Enter task description: ");
        newTask.setDescription(scanner.nextLine());
        System.out.print("Enter task priority level (low, medium, high): ");
        newTask.setPriorityLevel(scanner.nextLine());
        System.out.print("Enter task due date (YYYY-MM-DD): ");
        String dueDateInput = scanner.nextLine();
        try {
            newTask.setDuedate(LocalDate.parse(dueDateInput));
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Task will be created without a due date.");   
        }
        tasks.add(newTask);
        logActivity("Created task: '" + newTask.getTitle() + "' with due date " + newTask.getDuedate());
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

    private void assignTaskToProject() {
        System.out.println("[Assign task to project] - functionality to be implemented.");
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
                    sortedByTags.sort(Comparator.comparing(task -> String.join(", ", task.getTags()),
                            Comparator.nullsLast(String::compareTo)));
                    for (Task t : sortedByTags) {
                        System.out.println("Task: " + t.getTitle());
                        System.out.println("Tags: " + String.join(", ", t.getTags()));
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
        System.out.print("Enter keyword to search in task title or description: ");
        String query = scanner.nextLine().trim().toLowerCase();
        if (query.isEmpty()) {
            System.out.println("Empty query. Please enter a keyword.");
            return;
        }

        List<Task> matchedTasks = new ArrayList<>();
        for (Task t : tasks) {
            String title = t.getTitle() == null ? "" : t.getTitle().toLowerCase();
            String desc = t.getDescription() == null ? "" : t.getDescription().toLowerCase();

            if (title.contains(query) || desc.contains(query)) {
                System.out.println(t.toString());
                matchedTasks.add(t);
            }
        }

        if (matchedTasks.isEmpty()) {
            System.out.println("No tasks found matching '" + query + "'.");
            return;
        }

        logActivity("Performed search with keyword: '" + query + "', results: " + matchedTasks.size());

        System.out.print("Export this search result to CSV? (y/n): ");
        String exportChoice = scanner.nextLine().trim().toLowerCase();
        if (exportChoice.startsWith("y")) {
            exportTasksToCSV(matchedTasks);
        }
    }

    private void viewTaskHistory() {
        if (activityHistory.isEmpty()) {
            System.out.println("No task activity history available.");
            return;
        }
        System.out.println("Task activity history:");
        for (String entry : activityHistory) {
            System.out.println(entry);
        }
    }

    private void logActivity(String description) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        activityHistory.add(timestamp + " - " + description);
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
                            task.addSubtask(new Subtask(subtaskName, ""));
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

    public ArrayList<Task> getTasks() {
        return tasks;
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

