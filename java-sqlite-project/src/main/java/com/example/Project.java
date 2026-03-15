package com.example;
import java.util.ArrayList;

public class Project {
    private String title; 
    private String description; 
    private ArrayList<Task> tasks = new ArrayList<>();

    public String getTitle() {
        return title; 
    }

    public String setTitle(String title){
        this.title = title;
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String setDescription(String description) {
        this.description = description;
        return description;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }
    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Task t) {
        tasks.add(t);
    }

    public void removeTask(Task t) {
        tasks.remove(t);
    }

    //default constructor
    public Project() {  
    }
    // Constructor with paramaters for title and description
    public Project(String title, String description) {
        this.title = title;
        this.description = description; 
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project Title: ").append(title).append("\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Tasks:\n");
        for (Task t : tasks) {
            sb.append(t.toString()).append("\n");
        }
        return sb.toString();
}
}
