package com.example.foodbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Restaurants extends AppCompatActivity implements RestaurantAdapter.OnRestaurantClickListener {
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private EditText searchEditText;
    private TextView greetingText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ImageButton accountIcon;
    private PopupMenu popupMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        recyclerView = findViewById(R.id.restaurants_recycler_view);
        searchEditText = findViewById(R.id.search_edit_text);
        greetingText = findViewById(R.id.greeting_text);

        accountIcon = findViewById(R.id.account_icon);
        setupAccountMenu();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RestaurantAdapter(this, this);
        recyclerView.setAdapter(adapter);

        // Initialize chatbot FAB
        FloatingActionButton chatbotFab = findViewById(R.id.chatbot_fab);
        chatbotFab.setOnClickListener(v -> {
            startActivity(new Intent(Restaurants.this, chatbot.class));
        });



        setupChatbotFab();
        // Load user name
        loadUserName();

        // Load restaurants
        loadRestaurants();

        // Set up search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchRestaurants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }




    private void setupAccountMenu() {
        accountIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(Restaurants.this, accountIcon);
            popup.getMenuInflater().inflate(R.menu.dropmenu, popup.getMenu());

            // Set click listener for menu items
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.view_profile) {
                    // Navigate to Profile screen
                    startActivity(new Intent(Restaurants.this, Profile.class));
                    return true;
                } else if (itemId == R.id.logout) {
                    // Handle logout
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(Restaurants.this, SIgnIn.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }
    @Override
    public void onRestaurantClick(Restaurant restaurant) {
        Intent intent = new Intent(this, RestaurantMenu.class);
        intent.putExtra("restaurantId", restaurant.getId());
        intent.putExtra("restaurantName", restaurant.getName());
        startActivity(intent);
    }

    private void loadUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = mDatabase.child("users").child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            greetingText.setText("Hello " + name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Restaurants.this,
                            "Failed to load user name: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadRestaurants() {
        DatabaseReference restaurantsRef = mDatabase.child("restaurants");
        restaurantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Restaurant> restaurants = new ArrayList<>();
                for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {
                    Restaurant restaurant = restaurantSnapshot.getValue(Restaurant.class);
                    if (restaurant != null) {
                        restaurant.setId(restaurantSnapshot.getKey());
                        restaurants.add(restaurant);
                    }
                }
                adapter.setRestaurants(restaurants);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Restaurants.this,
                        "Failed to load restaurants: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchRestaurants(String query) {
        if (query.isEmpty()) {
            loadRestaurants();
            return;
        }

        query = query.toLowerCase().trim();
        DatabaseReference restaurantsRef = mDatabase.child("restaurants");
        String finalQuery = query;
        restaurantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Restaurant> filteredList = new ArrayList<>();
                for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {
                    Restaurant restaurant = restaurantSnapshot.getValue(Restaurant.class);
                    if (restaurant != null) {
                        if (restaurant.getName().toLowerCase().contains(finalQuery) ||
                                restaurant.getFoodType().toLowerCase().contains(finalQuery) ||
                                restaurant.getLocation().toLowerCase().contains(finalQuery)) {
                            restaurant.setId(restaurantSnapshot.getKey());
                            filteredList.add(restaurant);
                        }
                    }
                }
                adapter.setRestaurants(filteredList);

                if (filteredList.isEmpty()) {
                    Toast.makeText(Restaurants.this,
                            "No restaurants found matching your search",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Restaurants.this,
                        "Search failed: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupChatbotFab() {
        FloatingActionButton chatbotFab = findViewById(R.id.chatbot_fab);

        // Add pulse animation
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(chatbotFab, "scaleX", 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(chatbotFab, "scaleY", 0.9f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(chatbotFab, "scaleX", 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(chatbotFab, "scaleY", 1f);

        scaleDownX.setDuration(1000);
        scaleDownY.setDuration(1000);
        scaleUpX.setDuration(1000);
        scaleUpY.setDuration(1000);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.play(scaleUpX).with(scaleUpY);

        scaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleUp.start();
            }
        });

        scaleUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleDown.start();
            }
        });

        scaleDown.start();

        // Click handler
        chatbotFab.setOnClickListener(v -> {
            // Add click animation
            chatbotFab.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        chatbotFab.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    startActivity(new Intent(Restaurants.this, chatbot.class));
                                });
                    });
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Handle not signed in state
            finish(); // Close this activity
        }
    }
}