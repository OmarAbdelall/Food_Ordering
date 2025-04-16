package com.example.foodbot;

import java.util.Map;

public class Restaurant {
    private String id;
    private String name;
    private String imageUrl;
    private String foodType;
    private String location;
    private float averageRating;
    private int totalRatings;
    private Map<String, MenuItem> menu;  // Changed from List to Map

    // Empty constructor required for Firebase
    public Restaurant() {}

    // Constructor
    public Restaurant(String id, String name, String imageUrl, String foodType, String location) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.foodType = foodType;
        this.location = location;
        this.averageRating = 0;
        this.totalRatings = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

    public int getTotalRatings() { return totalRatings; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }

    public Map<String, MenuItem> getMenu() { return menu; }
    public void setMenu(Map<String, MenuItem> menu) { this.menu = menu; }
}