package com.example.foodbot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
    private List<MenuItem> menuItems;
    private Map<MenuItem, String> menuItemIds;
    private Context context;
    private String restaurantId;
    private String restaurantName;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public MenuAdapter(Context context, String restaurantId, String restaurantName) {
        this.context = context;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.menuItems = new ArrayList<>();
        this.menuItemIds = new HashMap<>();
        this.mAuth = FirebaseAuth.getInstance();
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);

        // Set name with null check
        holder.name.setText(menuItem.getName() != null ? menuItem.getName() : "");

        // Set calories with null check
        holder.calories.setText(menuItem.getCalories() + " calories");

        // Set price with null check
        holder.price.setText(String.format("$%.2f", menuItem.getPrice()));

        // Handle null lists for allergens, dietary, and ingredients
        List<String> allergens = menuItem.getAllergens();
        List<String> dietary = menuItem.getDietary();
        List<String> ingredients = menuItem.getIngredients();

        // Set allergens text with null check
        String allergensText = "Allergens: " +
                (allergens != null && !allergens.isEmpty() ? String.join(", ", allergens) : "None");
        holder.allergens.setText(allergensText);

        // Set dietary text with null check
        String dietaryText = "Dietary: " +
                (dietary != null && !dietary.isEmpty() ? String.join(", ", dietary) : "None");
        holder.dietary.setText(dietaryText);

        // Set ingredients text with null check
        String ingredientsText = "Ingredients: " +
                (ingredients != null && !ingredients.isEmpty() ? String.join(", ", ingredients) : "None");
        holder.ingredients.setText(ingredientsText);

        // Load image with null check
        if (menuItem.getImageUrl() != null) {
            Glide.with(context)
                    .load(menuItem.getImageUrl())
                    .into(holder.image);
        }


        holder.addToCartButton.setOnClickListener(v -> {
            String userId = mAuth.getCurrentUser().getUid();
            mDatabase.child("users").child(userId).child("cart").limitToFirst(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                CartItem existingItem = snapshot.getChildren().iterator().next().getValue(CartItem.class);
                                if (existingItem != null && !existingItem.getRestaurantId().equals(restaurantId)) {
                                    Toast.makeText(context,
                                            "Please complete or clear your existing cart first",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                            showAddToCartDialog(menuItem);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        });
    }

    private void showAddToCartDialog(MenuItem menuItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.add_to_cart_dialog, null);

        TextView titleText = dialogView.findViewById(R.id.dialog_title);
        TextView priceText = dialogView.findViewById(R.id.dialog_price);

        titleText.setText("Add " + (menuItem.getName() != null ? menuItem.getName() : "") + " to cart?");
        priceText.setText(String.format("Price: $%.2f", menuItem.getPrice()));

        builder.setView(dialogView)
                .setPositiveButton("Add to Cart", (dialog, which) -> addItemToCart(menuItem))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addItemToCart(MenuItem menuItem) {
        String userId = mAuth.getCurrentUser().getUid();
        String cartItemId = mDatabase.child("users").child(userId).child("cart").push().getKey();

        CartItem cartItem = new CartItem(
                cartItemId,
                menuItem.getName(),
                menuItem.getPrice(),
                restaurantId,
                restaurantName
        );

        mDatabase.child("users").child(userId).child("cart").child(cartItemId).setValue(cartItem)
                .addOnSuccessListener(aVoid -> Toast.makeText(context,
                        "Added to cart", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context,
                        "Failed to add to cart", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public void setMenuItems(List<MenuItem> menuItems, Map<MenuItem, String> menuItemIds) {
        this.menuItems = menuItems != null ? menuItems : new ArrayList<>();
        this.menuItemIds = menuItemIds != null ? menuItemIds : new HashMap<>();
        notifyDataSetChanged();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, calories, allergens, dietary, ingredients, price;
        ImageButton addToCartButton;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.dish_image);
            name = itemView.findViewById(R.id.dish_name);
            calories = itemView.findViewById(R.id.dish_calories);
            allergens = itemView.findViewById(R.id.dish_allergens);
            dietary = itemView.findViewById(R.id.dish_dietary);
            ingredients = itemView.findViewById(R.id.dish_ingredients);
            price = itemView.findViewById(R.id.dish_price);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_button);
        }
    }
}