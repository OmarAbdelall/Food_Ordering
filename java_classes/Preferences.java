package com.example.foodbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Preferences extends AppCompatActivity {
    // Checkboxes for Allergies
    private CheckBox milkCheckbox, eggsCheckbox, peanutsCheckbox, fishCheckbox;

    // Checkboxes for Dietary Preferences
    private CheckBox vegetarianCheckbox, veganCheckbox, halalCheckbox, glutenFreeCheckbox;

    private Button saveButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        // Initialize Allergies checkboxes
        milkCheckbox = findViewById(R.id.milk_checkbox);
        eggsCheckbox = findViewById(R.id.eggs_checkbox);
        peanutsCheckbox = findViewById(R.id.peanuts_checkbox);
        fishCheckbox = findViewById(R.id.fish_checkbox);

        // Initialize Dietary Preferences checkboxes
        vegetarianCheckbox = findViewById(R.id.vegetarian_checkbox);
        veganCheckbox = findViewById(R.id.vegan_checkbox);
        halalCheckbox = findViewById(R.id.halal_checkbox);
        glutenFreeCheckbox = findViewById(R.id.gluten_free_checkbox);

        // Initialize Save button
        saveButton = findViewById(R.id.save_preferences_button);

        // Set up checkbox listeners for Vegan and Vegetarian
        veganCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && vegetarianCheckbox.isChecked()) {
                vegetarianCheckbox.setChecked(false);
                Toast.makeText(Preferences.this,
                        "You cannot select both Vegan and Vegetarian",
                        Toast.LENGTH_SHORT).show();
            }
        });

        vegetarianCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && veganCheckbox.isChecked()) {
                veganCheckbox.setChecked(false);
                Toast.makeText(Preferences.this,
                        "You cannot select both Vegan and Vegetarian",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Load existing preferences
        loadExistingPreferences();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
            }
        });
    }






    private void loadExistingPreferences() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid())
                    .child("preferences")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Load allergies
                                DataSnapshot allergiesSnapshot = snapshot.child("allergies");
                                if (allergiesSnapshot.exists()) {
                                    for (DataSnapshot allergy : allergiesSnapshot.getChildren()) {
                                        String allergyValue = allergy.getValue(String.class);
                                        if (allergyValue != null) {
                                            switch (allergyValue) {
                                                case "Milk":
                                                    milkCheckbox.setChecked(true);
                                                    break;
                                                case "Eggs":
                                                    eggsCheckbox.setChecked(true);
                                                    break;
                                                case "Peanuts":
                                                    peanutsCheckbox.setChecked(true);
                                                    break;
                                                case "Fish":
                                                    fishCheckbox.setChecked(true);
                                                    break;
                                            }
                                        }
                                    }
                                }

                                // Load dietary preferences
                                DataSnapshot dietarySnapshot = snapshot.child("dietaryPreferences");
                                if (dietarySnapshot.exists()) {
                                    for (DataSnapshot preference : dietarySnapshot.getChildren()) {
                                        String preferenceValue = preference.getValue(String.class);
                                        if (preferenceValue != null) {
                                            switch (preferenceValue) {
                                                case "Vegetarian":
                                                    vegetarianCheckbox.setChecked(true);
                                                    break;
                                                case "Vegan":
                                                    veganCheckbox.setChecked(true);
                                                    break;
                                                case "Halal":
                                                    halalCheckbox.setChecked(true);
                                                    break;
                                                case "Gluten Free":
                                                    glutenFreeCheckbox.setChecked(true);
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Preferences.this,
                                    "Failed to load preferences: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void savePreferences() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user preferences
        Map<String, Object> preferences = new HashMap<>();

        // Get allergies
        ArrayList<String> allergies = new ArrayList<>();
        if (milkCheckbox.isChecked()) allergies.add("Milk");
        if (eggsCheckbox.isChecked()) allergies.add("Eggs");
        if (peanutsCheckbox.isChecked()) allergies.add("Peanuts");
        if (fishCheckbox.isChecked()) allergies.add("Fish");

        // Get dietary preferences
        ArrayList<String> dietaryPreferences = new ArrayList<>();
        if (vegetarianCheckbox.isChecked()) dietaryPreferences.add("Vegetarian");
        if (veganCheckbox.isChecked()) dietaryPreferences.add("Vegan");
        if (halalCheckbox.isChecked()) dietaryPreferences.add("Halal");
        if (glutenFreeCheckbox.isChecked()) dietaryPreferences.add("Gluten Free");

        // Add to preferences map
        preferences.put("allergies", allergies);
        preferences.put("dietaryPreferences", dietaryPreferences);

        // Save to Firebase
        mDatabase.child("users").child(currentUser.getUid()).child("preferences")
                .setValue(preferences)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Preferences.this,
                                    "Preferences saved successfully!",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Preferences.this, Restaurants.class));
                            finish();
                        } else {
                            Toast.makeText(Preferences.this,
                                    "Failed to save preferences",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}