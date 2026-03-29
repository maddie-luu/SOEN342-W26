package com.example;

public class Collaborator {
    private String name;
    private String category; // Senior, Intermediate, Junior

    public Collaborator() {}

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

    public int getOpenTaskLimit() {
        if (category == null) return Integer.MAX_VALUE;
        switch (category.toLowerCase()) {
            case "junior":
                return 10;
            case "intermediate":
                return 5;
            case "senior":
                return 2;
            default:
                return Integer.MAX_VALUE;
        }
    }

    @Override
    public String toString() {
        return "Collaborator{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
