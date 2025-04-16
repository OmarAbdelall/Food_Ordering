package com.example.foodbot;

public class OrderHistoryItem {
    private String orderId;
    private String restaurantId;
    private String restaurantName;
    private String mealId;
    private String mealName;
    private double price;
    private long timestamp;

    // Required empty constructor for Firebase
    public OrderHistoryItem() {}

    public OrderHistoryItem(String orderId, String restaurantId, String restaurantName,
                            String mealId, String mealName, double price, long timestamp) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.mealId = mealId;
        this.mealName = mealName;
        this.price = price;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getMealId() { return mealId; }
    public void setMealId(String mealId) { this.mealId = mealId; }

    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}