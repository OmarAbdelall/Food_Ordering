package com.example.foodbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class RestaurantMenu extends AppCompatActivity {
    private String restaurantId;
    private String restaurantName;
    private TextView restaurantNameText;
    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private DatabaseReference mDatabase;
    private TextView cartBadge;
    private EditText searchEditText;
    private List<MenuItem> allMenuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_menu);

        restaurantId = getIntent().getStringExtra("restaurantId");
        restaurantName = getIntent().getStringExtra("restaurantName");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        restaurantNameText = findViewById(R.id.restaurant_name);
        menuRecyclerView = findViewById(R.id.menu_recycler_view);
        cartBadge = findViewById(R.id.cart_badge);
        searchEditText = findViewById(R.id.search_edit_text);
        ImageButton backArrow = findViewById(R.id.back_arrow);
        ImageButton cartButton = findViewById(R.id.cart_button);

        restaurantNameText.setText(restaurantName);

        // Setup RecyclerView
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuAdapter(this, restaurantId, restaurantName);
        menuRecyclerView.setAdapter(menuAdapter);

        // Setup click listeners
        backArrow.setOnClickListener(v -> finish());
        cartButton.setOnClickListener(v -> startActivity(new Intent(RestaurantMenu.this, Cart.class)));

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenuItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initial load of menu items
        loadMenuItems();
        updateCartBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMenuItems();
        updateCartBadge();
    }

    private void loadMenuItems() {
        DatabaseReference menuRef = mDatabase.child("restaurants").child(restaurantId).child("menu");
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MenuItem> menuItems = new ArrayList<>();
                Map<MenuItem, String> menuItemIds = new HashMap<>();

                for (DataSnapshot menuSnapshot : snapshot.getChildren()) {
                    MenuItem menuItem = menuSnapshot.getValue(MenuItem.class);
                    if (menuItem != null) {
                        menuItems.add(menuItem);
                        menuItemIds.put(menuItem, menuSnapshot.getKey());
                    }
                }

                allMenuItems = new ArrayList<>(menuItems);
                menuAdapter.setMenuItems(menuItems, menuItemIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantMenu.this,
                        "Failed to load menu: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartBadge() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long itemCount = snapshot.getChildrenCount();
                if (itemCount > 0) {
                    cartBadge.setVisibility(View.VISIBLE);
                    cartBadge.setText(String.valueOf(itemCount));
                } else {
                    cartBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void filterMenuItems(String query) {
        if (allMenuItems == null) return;

        query = query.toLowerCase().trim();
        List<MenuItem> filteredList = new ArrayList<>();
        Map<MenuItem, String> filteredIds = new HashMap<>();

        if (query.isEmpty()) {
            filteredList.addAll(allMenuItems);
            for (MenuItem item : allMenuItems) {
                filteredIds.put(item, item.getName());
            }
        } else {
            for (MenuItem item : allMenuItems) {
                if (item.getName().toLowerCase().contains(query)) {
                    filteredList.add(item);
                    filteredIds.put(item, item.getName());
                }
            }
        }

        menuAdapter.setMenuItems(filteredList, filteredIds);
    }
}