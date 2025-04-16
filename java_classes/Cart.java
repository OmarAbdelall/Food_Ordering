package com.example.foodbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cart extends AppCompatActivity {
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView emptyCartText, totalText;
    private Button checkoutButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private double total = 0.0;
    private String currentRestaurantId;

    private String generateMealId(String restaurantId, String mealName) {
        String cleanMealName = mealName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return restaurantId + "_" + cleanMealName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        cartRecyclerView = findViewById(R.id.cart_recycler_view);
        emptyCartText = findViewById(R.id.empty_cart_text);
        totalText = findViewById(R.id.total_text);
        checkoutButton = findViewById(R.id.checkout_button);
        ImageButton backArrow = findViewById(R.id.back_arrow);

        // Setup RecyclerView
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this);
        cartRecyclerView.setAdapter(cartAdapter);

        // Back button click
        backArrow.setOnClickListener(v -> finish());

        // Checkout button click
        checkoutButton.setOnClickListener(v -> processCheckout());

        // Load cart items
        loadCartItems();
    }

    public void updateTotal() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference cartRef = mDatabase.child("users").child(userId).child("cart");

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double newTotal = 0.0;
                List<CartItem> updatedItems = new ArrayList<>();

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        updatedItems.add(cartItem);
                        newTotal += cartItem.getPrice();
                    }
                }

                total = newTotal;
                updateUI(updatedItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Cart.this,
                        "Failed to update cart: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCartItems() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference cartRef = mDatabase.child("users").child(userId).child("cart");

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CartItem> cartItems = new ArrayList<>();
                total = 0.0;

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        cartItems.add(cartItem);
                        total += cartItem.getPrice();
                        currentRestaurantId = cartItem.getRestaurantId();
                    }
                }

                updateUI(cartItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Cart.this,
                        "Failed to load cart items: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            emptyCartText.setVisibility(View.VISIBLE);
            cartRecyclerView.setVisibility(View.GONE);
            checkoutButton.setEnabled(false);
            totalText.setText("Total: $0.00");
        } else {
            emptyCartText.setVisibility(View.GONE);
            cartRecyclerView.setVisibility(View.VISIBLE);
            checkoutButton.setEnabled(true);
            totalText.setText(String.format("Total: $%.2f", total));
            cartAdapter.setItems(cartItems);
        }
    }

    private void processCheckout() {
        if (currentRestaurantId == null) {
            Toast.makeText(this, "Error: Restaurant ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference cartRef = mDatabase.child("users").child(userId).child("cart");

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // First, save to order history
                DatabaseReference historyRef = mDatabase.child("users").child(userId)
                        .child("stats").child("orderHistory");

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        // Create history entry
                        String orderId = historyRef.push().getKey();

                        OrderHistoryItem historyItem = new OrderHistoryItem(
                                orderId,
                                cartItem.getRestaurantId(),
                                cartItem.getRestaurantName(),
                                cartItem.getMenuItemId(),
                                cartItem.getName(),
                                cartItem.getPrice(),
                                TimeUtils.getCurrentTimestamp()
                        );

                        // Save to history
                        if (orderId != null) {
                            historyRef.child(orderId).setValue(historyItem);
                        }

                        // Update aggregated stats
                        updateOrderStats(userId, cartItem);

                        // Increment meal order count
                        incrementMealOrderCount(cartItem);
                    }
                }

                // Clear cart after saving history
                cartRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showRatingDialog();
                    } else {
                        Toast.makeText(Cart.this, "Failed to clear cart", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Cart.this, "Checkout failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderStats(String userId, CartItem cartItem) {
        DatabaseReference statsRef = mDatabase.child("users").child(userId)
                .child("stats").child("aggregatedStats").child("restaurants");
        String restaurantId = cartItem.getRestaurantId();
        String cleanMealName = cartItem.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        // First check if restaurant exists in user's stats
        statsRef.child(restaurantId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // First time order from this restaurant - create restaurant node
                    Map<String, Object> restaurantData = new HashMap<>();
                    restaurantData.put("restaurantName", cartItem.getRestaurantName());
                    restaurantData.put("totalOrders", 1);

                    // Create initial meal data
                    Map<String, Object> mealData = new HashMap<>();
                    mealData.put("mealName", cartItem.getName());
                    mealData.put("totalOrders", 1);

                    restaurantData.put("meals/" + cleanMealName, mealData);

                    statsRef.child(restaurantId).updateChildren(restaurantData)
                            .addOnFailureListener(e ->
                                    Toast.makeText(Cart.this,
                                            "Failed to create restaurant stats",
                                            Toast.LENGTH_SHORT).show());
                } else {
                    // Restaurant exists - update counters

                    // Update restaurant total orders
                    long currentRestaurantOrders = snapshot.child("totalOrders").getValue(Long.class) != null ?
                            snapshot.child("totalOrders").getValue(Long.class) : 0;
                    statsRef.child(restaurantId).child("totalOrders")
                            .setValue(currentRestaurantOrders + 1);

                    // Check if meal exists
                    DataSnapshot mealSnapshot = snapshot.child("meals").child(cleanMealName);
                    if (!mealSnapshot.exists()) {
                        // First time order for this meal
                        Map<String, Object> mealData = new HashMap<>();
                        mealData.put("mealName", cartItem.getName());
                        mealData.put("totalOrders", 1);

                        statsRef.child(restaurantId)
                                .child("meals").child(cleanMealName)
                                .updateChildren(mealData);
                    } else {
                        // Meal exists - update its counter
                        long currentMealOrders = mealSnapshot.child("totalOrders").getValue(Long.class) != null ?
                                mealSnapshot.child("totalOrders").getValue(Long.class) : 0;
                        statsRef.child(restaurantId)
                                .child("meals").child(cleanMealName)
                                .child("totalOrders")
                                .setValue(currentMealOrders + 1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Cart.this,
                        "Failed to update order stats",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void incrementMealOrderCount(CartItem cartItem) {
        DatabaseReference menuRef = mDatabase.child("restaurants")
                .child(cartItem.getRestaurantId())
                .child("menu");

        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot menuItemSnapshot : snapshot.getChildren()) {
                    MenuItem menuItem = menuItemSnapshot.getValue(MenuItem.class);
                    if (menuItem != null && menuItem.getName().equals(cartItem.getName())) {
                        int newOrderCount = menuItem.getOrderCount() + 1;
                        menuRef.child(menuItemSnapshot.getKey()).child("orderCount").setValue(newOrderCount);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Cart", "Failed to increment meal count", error.toException());
            }
        });
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.rating_dialog, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);

        builder.setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    updateRestaurantRating(rating);
                })
                .setNegativeButton("Skip", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void updateRestaurantRating(float newRating) {
        DatabaseReference restaurantRef = mDatabase.child("restaurants").child(currentRestaurantId);

        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Restaurant restaurant = snapshot.getValue(Restaurant.class);
                if (restaurant != null) {
                    float currentAverage = restaurant.getAverageRating();
                    int totalRatings = restaurant.getTotalRatings();
                    float newAverage = ((currentAverage * totalRatings) + newRating) / (totalRatings + 1);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("averageRating", newAverage);
                    updates.put("totalRatings", totalRatings + 1);

                    restaurantRef.updateChildren(updates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Cart.this, "Thank you for your rating!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Cart.this, "Failed to update rating", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Cart.this, "Failed to update rating", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}