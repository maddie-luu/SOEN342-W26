package com.example;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Simple command-line interface for task and project management.
 * This is the first step: providing the user menu and
 * wiring up placeholders for the required actions.
 */
public class TaskManagementCLI {

    private final Scanner scanner;

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
        System.out.println("[Create task] - functionality to be implemented.");
    }

    private void createProject() {
        System.out.println("[Create project] - functionality to be implemented.");
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
        System.out.println("[View tasks] - functionality to be implemented.");
    }

    private void searchTasks() {
        System.out.println("[Search tasks] - functionality to be implemented.");
    }

    private void viewTaskHistory() {
        System.out.println("[History of task-related activities] - functionality to be implemented.");
    }
}
