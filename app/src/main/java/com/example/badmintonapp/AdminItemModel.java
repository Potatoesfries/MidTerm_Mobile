package com.example.badmintonapp;

public class AdminItemModel {
    private long id;
    private String subject;
    private String description;
    private String category;
    private double price;

    // Constructor for new items
    public AdminItemModel(String subject, String description, String category, double price) {
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.price = price;
    }

    // Constructor for existing items
    public AdminItemModel(long id, String subject, String description, String category, double price) {
        this.id = id;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.price = price;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return subject;
    }
}
