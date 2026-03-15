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
    public final ArrayList<task> tasks = new ArrayList<>();    
    public final ArrayList<Project> projects = new ArrayList<>();

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
        task newTask = new task();
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
        System.out.println("[Edit task] - functionality to be implemented.");
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
                task t = tasks.get(i);
                System.out.print(t.toString());
            }
        }
    }

    private void searchTasks() {
        System.out.println("[Search tasks] - functionality to be implemented.");
    }

    private void viewTaskHistory() {
        System.out.println("[History of task-related activities] - functionality to be implemented.");
    }

    public ArrayList<task> getTasks() {
        return tasks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (task t : tasks) {
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

