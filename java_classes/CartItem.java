package com.example.foodbot;

public class CartItem {
    private String menuItemId;
    private String name;
    private double price;
    private String restaurantId;
    private String restaurantName;

    public CartItem() {}

    public CartItem(String menuItemId, String name, double price, String restaurantId, String restaurantName) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
    }

    // Getters and setters
    public String getMenuItemId() { return menuItemId; }
    public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
}