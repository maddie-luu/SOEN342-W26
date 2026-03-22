package com.example;

/**
 * Represents a collaborator assigned to a task.
 * A collaborator has a name and a category (e.g., "developer", "reviewer", "manager").
 */
public class Collaborator {
    private String name;
    private String category;

    // Default constructor
    public Collaborator() {
    }

    // Constructor with parameters
    public Collaborator(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return name + " (" + category + ")";
    }
}
