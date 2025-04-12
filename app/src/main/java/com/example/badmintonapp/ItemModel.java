package com.example.badmintonapp;

public class ItemModel {
    private long id;
    private String name;
    private String description;
    private String category;
    private double price;
    private int image;

    // Full constructor
    public ItemModel(long id, String name, String description, String category, double price, int image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.image = image;
    }

    // Constructor without price (for backward compatibility)
    public ItemModel(long id, String name, String description, String category, int image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = 0.0; // Default price
        this.image = image;
    }

    // Simple constructor for search results
    public ItemModel(String name, int image, String description) {
        this.id = -1; // Default ID
        this.name = name;
        this.description = description;
        this.category = "Racket"; // Default category
        this.price = 0.0; // Default price
        this.image = image;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return name;
    }
}
