package com.example;

public class Collaborator {
    private String name;
    private String category; // Senior, Intermediate, Junior
    // US-17: Custom limit for collaborator category (must be positive integer)
    private Integer customOpenTaskLimit = null;

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

    /**
     * US-17: Get custom open task limit if set, otherwise return default based on category
     */
    public Integer getCustomOpenTaskLimit() {
        return customOpenTaskLimit;
    }

    /**
     * US-17: Set custom open task limit with validation
     * Only accepts positive integers
     */
    public void setCustomOpenTaskLimit(Integer limit) throws IllegalArgumentException {
        if (limit != null && limit <= 0) {
            throw new IllegalArgumentException("Collaborator category limit must be a positive integer. Got: " + limit);
        }
        this.customOpenTaskLimit = limit;
    }

    public int getOpenTaskLimit() {
        // If custom limit is set, use it (validated during setCustomOpenTaskLimit)
        if (customOpenTaskLimit != null) {
            return customOpenTaskLimit;
        }
        
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
