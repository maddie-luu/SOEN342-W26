package com.example;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
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
    
    // TaskService handles task operations and activity tracking
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
        System.out.print("Enter task title: ");
        String title = scanner.nextLine();
        System.out.print("Enter task description: ");
        String description = scanner.nextLine();
        System.out.print("Enter task priority level (low, medium, high): ");
        String priorityLevel = scanner.nextLine();
        System.out.print("Enter task due date (YYYY-MM-DD): ");
        String dueDateInput = scanner.nextLine();
        
        LocalDate dueDate = null;
        try {
            if (!dueDateInput.trim().isEmpty()) {
                dueDate = LocalDate.parse(dueDateInput);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Task will be created without a due date.");   
        }
        
        // Use TaskService to create task (automatically records activity)
        Task newTask = taskService.createTask(title, description, priorityLevel, dueDate);
        System.out.println("✓ Task created successfully with ID: " + newTask.getId());
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
        if (tasks.isEmpty()) {
            System.out.println("No tasks available to edit.");
            return;
        }
        
        System.out.println("Which task would you like to edit? (Enter task number)");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            System.out.println((i + 1) + ". [ID:" + t.getId() + "] " + t.getTitle());
        }
        
        int taskNumber = readInt("Enter task number: ");
        if (taskNumber >= 1 && taskNumber <= tasks.size()) {
            Task task = tasks.get(taskNumber - 1);
            int taskId = task.getId();
            
            System.out.println("What would you like to edit?");
            System.out.println("1. Title");
            System.out.println("2. Description");
            System.out.println("3. Priority Level");
            System.out.println("4. Due date");
            System.out.println("5. Mark as Completed");
            System.out.println("6. Mark as Cancelled");
            System.out.println("7. Tags");

            int editChoice = readInt("Enter your choice: ");
            switch (editChoice) {
                case 1:
                    System.out.print("Enter new task title: ");
                    String newTitle = scanner.nextLine();
                    taskService.updateTask(taskId, newTitle, null, null, null);
                    System.out.println("✓ Task title updated.");
                    break;
                case 2:
                    System.out.print("Enter new task description: ");
                    String newDesc = scanner.nextLine();
                    taskService.updateTask(taskId, null, newDesc, null, null);
                    System.out.println("✓ Task description updated.");
                    break;
                case 3:
                    System.out.print("Enter new task priority level (low, medium, high): ");
                    String newPriority = scanner.nextLine();
                    taskService.updateTask(taskId, null, null, newPriority, null);
                    System.out.println("✓ Task priority updated.");
                    break;
                case 4:
                    System.out.print("Enter new task due date (YYYY-MM-DD): ");
                    String dueDateInput = scanner.nextLine();
                    try {
                        LocalDate newDueDate = LocalDate.parse(dueDateInput);
                        taskService.updateTask(taskId, null, null, null, newDueDate);
                        System.out.println("✓ Task due date updated.");
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Due date will not be updated.");
                    }
                    break;
                case 5:
                    // Mark as completed using TaskService (records activity)
                    if (taskService.completeTask(taskId)) {
                        System.out.println("✓ Task marked as completed.");
                    } else {
                        System.out.println("✗ Could not complete task. It may already be completed or cancelled.");
                    }
                    break;
                case 6:
                    // Mark as cancelled using TaskService (records activity)
                    if (taskService.cancelTask(taskId)) {
                        System.out.println("✓ Task marked as cancelled.");
                    } else {
                        System.out.println("✗ Could not cancel task. It may already be completed or cancelled.");
                    }
                    break;
                case 7:
                    System.out.println("[Edit tags] - functionality to be implemented.");
                    break;  
            
                default:
                    System.out.println("Invalid choice.");
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
        System.out.println("[Search tasks] - functionality to be implemented.");
    }

    private void viewTaskHistory() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        
        System.out.println("Select a task to view its activity history:");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            System.out.println((i + 1) + ". [ID:" + t.getId() + "] " + t.getTitle() + " (" + t.getStatus() + ")");
        }
        
        int taskNumber = readInt("Enter task number: ");
        if (taskNumber >= 1 && taskNumber <= tasks.size()) {
            Task task = tasks.get(taskNumber - 1);
            // Use TaskService to print the activity history
            taskService.printTaskActivityHistory(task.getId());
        } else {
            System.out.println("Invalid task number.");
        }
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

