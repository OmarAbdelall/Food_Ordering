package com.example.foodbot;

import java.util.List;

public class MenuItem {
    private String name;
    private double price;
    private int calories;
    private List<String> allergens;
    private List<String> dietary;
    private List<String> ingredients;
    private String imageUrl;
    private int orderCount;

    public MenuItem() {}

    public MenuItem(String name, double price, int calories, List<String> allergens,
                    List<String> dietary, List<String> ingredients, String imageUrl) {
        this.name = name;
        this.price = price;
        this.calories = calories;
        this.allergens = allergens;
        this.dietary = dietary;
        this.ingredients = ingredients;
        this.imageUrl = imageUrl;
        this.orderCount = 0;  // Initialize to 0
    }

    // Getters
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getCalories() { return calories; }
    public List<String> getAllergens() { return allergens; }
    public List<String> getDietary() { return dietary; }
    public List<String> getIngredients() { return ingredients; }
    public String getImageUrl() { return imageUrl; }

    public int getOrderCount() { return orderCount; }





    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }



    // Setters
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }
    public void setDietary(List<String> dietary) { this.dietary = dietary; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}