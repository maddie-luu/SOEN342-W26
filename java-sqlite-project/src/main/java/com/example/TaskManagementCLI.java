package com.example;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
    
    // TaskService handles search and other task-related business logic
    private final TaskService taskService;

    public TaskManagementCLI() {
        this.scanner = new Scanner(System.in);
        // Initialize TaskService with reference to the tasks list
        this.taskService = new TaskService(tasks);
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
        System.out.println("9. Exit");
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
                    task.setTitle(scanner.nextLine());
                    break;
                case 2:
                    System.out.print("Enter new task description: ");
                    task.setDescription(scanner.nextLine());
                    break;
                case 3:
                    System.out.print("Enter new task priority level (low, medium, high): ");
                    task.setPriorityLevel(scanner.nextLine());
                    break;
                case 4:
                    System.out.print("Enter new task due date (YYYY-MM-DD): ");
                    String dueDateInput = scanner.nextLine();
                    try {
                        task.setDuedate(LocalDate.parse(dueDateInput));
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Due date will not be updated.");
                    }
                    break;
                case 5:
                    System.out.print("Enter new task status (open, in progress, completed): ");
                    task.setStatus(scanner.nextLine());
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
        System.out.println("[Edit project] - functionality to be implemented.");
    }

    private void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
        } else {
            System.out.println("Tasks:");
            for (int i = 0; i < tasks.size(); i++) {
                Task t = tasks.get(i);
                System.out.print(t.toString());
            }
        }
    }

    private void searchTasks() {
        System.out.print("Enter search keyword (or press Enter to see all open tasks): ");
        String keyword = scanner.nextLine();

        // Use TaskService to perform the search
        List<Task> results = taskService.searchTasks(keyword);

        // Display search results
        if (results.isEmpty()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                System.out.println("No open tasks found.");
            } else {
                System.out.println("No tasks found matching: \"" + keyword + "\"");
            }
        } else {
            // Show result count and header
            if (keyword == null || keyword.trim().isEmpty()) {
                System.out.println("\n=== All Open Tasks (sorted by due date) ===");
            } else {
                System.out.println("\n=== Search Results for: \"" + keyword + "\" ===");
            }
            System.out.println("Found " + results.size() + " task(s):\n");

            // Display each matching task
            for (int i = 0; i < results.size(); i++) {
                Task t = results.get(i);
                System.out.println((i + 1) + ". " + t.toString());
            }

            // Offer CSV export option after displaying results
            offerCSVExport(results);
        }
    }

    /**
     * Offers the user the option to export search results to a CSV file.
     * Called after search results are displayed.
     * 
     * @param tasks The list of tasks to potentially export
     */
    private void offerCSVExport(List<Task> tasks) {
        System.out.print("Would you like to export these results to CSV? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("y") || response.equals("yes")) {
            System.out.print("Enter file path (e.g., tasks_export.csv): ");
            String filePath = scanner.nextLine().trim();

            // Default filename if user presses enter without input
            if (filePath.isEmpty()) {
                filePath = "tasks_export.csv";
            }

            // Attempt to export to CSV
            try {
                CSVExporter.exportTasksToCSV(tasks, filePath);
                System.out.println("✓ Successfully exported " + tasks.size() + " task(s) to: " + filePath);
            } catch (java.io.IOException e) {
                System.out.println("✗ Error exporting to CSV: " + e.getMessage());
            }
        }
    }

    private void viewTaskHistory() {
        System.out.println("[History of task-related activities] - functionality to be implemented.");
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

