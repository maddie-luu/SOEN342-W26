package com.example.model;
import java.util.ArrayList;

public class Project {
    private int id;
    private String title; 
    private String description; 
    private ArrayList<Task> tasks = new ArrayList<>();
    private ArrayList<Collaborator> collaborators = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public ArrayList<Collaborator> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(ArrayList<Collaborator> collaborators) {
        this.collaborators = collaborators;
    }

    public void addCollaborator(Collaborator collaborator) {
        if (collaborator == null || collaborator.getName() == null) {
            return;
        }
        if (getCollaboratorByName(collaborator.getName()) != null) {
            return;
        }
        this.collaborators.add(collaborator);
    }

    public Collaborator getCollaboratorByName(String name) {
        if (name == null) return null;
        for (Collaborator c : collaborators) {
            if (name.equalsIgnoreCase(c.getName())) {
                return c;
            }
        }
        return null;
    }

    public void addTask(Task t) {
        if (t == null || tasks.contains(t)) {
            return;
        }
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
