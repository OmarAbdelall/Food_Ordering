package com.example.foodbot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private Context context;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public CartAdapter(Context context) {
        this.context = context;
        this.cartItems = new ArrayList<>();
        this.mAuth = FirebaseAuth.getInstance();
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.nameText.setText(item.getName());
        holder.restaurantText.setText(item.getRestaurantName());
        holder.priceText.setText(String.format("$%.2f", item.getPrice()));

        holder.deleteButton.setOnClickListener(v -> removeFromCart(position));
    }

    private void removeFromCart(int position) {
        String userId = mAuth.getCurrentUser().getUid();
        // Updated path to users/userId/cart
        DatabaseReference cartRef = mDatabase.child("users").child(userId).child("cart");

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CartItem itemToRemove = cartItems.get(position);
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null && cartItem.getName().equals(itemToRemove.getName())) {
                        itemSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    cartItems.remove(position);
                                    notifyDataSetChanged();
                                    if (context instanceof Cart) {
                                        ((Cart) context).updateTotal();
                                    }
                                    Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context,
                                        "Failed to remove item", Toast.LENGTH_SHORT).show());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error removing item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setItems(List<CartItem> items) {
        this.cartItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, restaurantText, priceText;
        ImageButton deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.item_name);
            restaurantText = itemView.findViewById(R.id.restaurant_name);
            priceText = itemView.findViewById(R.id.item_price);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}