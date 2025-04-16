package com.example.foodbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView nameText, emailText, phoneText, profileInitial;
    private ChipGroup allergiesChipGroup, dietaryChipGroup;
    private ImageButton backButton, editNameButton, editPhoneButton, editPreferencesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // In Profile.java, add this to onCreate after initializing views
        ImageButton viewHistoryButton = findViewById(R.id.view_history);
        viewHistoryButton.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, History.class));
        });

        // Set click listeners
        setClickListeners();

        // Load user data
        loadUserData();
    }

    private void initializeViews() {
        nameText = findViewById(R.id.name_text);
        emailText = findViewById(R.id.email_text);
        phoneText = findViewById(R.id.phone_text);
        profileInitial = findViewById(R.id.profile_initial);
        allergiesChipGroup = findViewById(R.id.allergies_chip_group);
        dietaryChipGroup = findViewById(R.id.dietary_chip_group);
        backButton = findViewById(R.id.back_button);
        editNameButton = findViewById(R.id.edit_name);
        editPhoneButton = findViewById(R.id.edit_phone);
        editPreferencesButton = findViewById(R.id.edit_preferences);
    }

    private void setClickListeners() {
        backButton.setOnClickListener(v -> finish());
        editNameButton.setOnClickListener(v -> showEditDialog("Name", nameText.getText().toString(), "name"));
        editPhoneButton.setOnClickListener(v -> showEditDialog("Phone", phoneText.getText().toString(), "phone"));
        editPreferencesButton.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Preferences.class);
            intent.putExtra("isEditing", true);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = mDatabase.child("users").child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);

                        // Set the values to TextViews
                        if (name != null) {
                            nameText.setText(name);
                            profileInitial.setText(name.substring(0, 1).toUpperCase());
                        }
                        if (email != null) {
                            emailText.setText(email);
                        }
                        if (phone != null) {
                            phoneText.setText(phone);
                        }

                        // Load preferences
                        DataSnapshot preferencesSnapshot = snapshot.child("preferences");
                        if (preferencesSnapshot.exists()) {
                            loadPreferences(preferencesSnapshot);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Profile.this,
                            "Failed to load profile: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void loadPreferences(DataSnapshot preferencesSnapshot) {
        allergiesChipGroup.removeAllViews();
        dietaryChipGroup.removeAllViews();

        if (preferencesSnapshot.exists()) {
            // Load allergies
            DataSnapshot allergiesSnapshot = preferencesSnapshot.child("allergies");
            if (allergiesSnapshot.exists()) {
                for (DataSnapshot allergy : allergiesSnapshot.getChildren()) {
                    String allergyValue = allergy.getValue(String.class);
                    if (allergyValue != null) {
                        addChip(allergiesChipGroup, allergyValue);
                    }
                }
            }

            // Load dietary preferences
            DataSnapshot dietarySnapshot = preferencesSnapshot.child("dietaryPreferences");
            if (dietarySnapshot.exists()) {
                for (DataSnapshot preference : dietarySnapshot.getChildren()) {
                    String preferenceValue = preference.getValue(String.class);
                    if (preferenceValue != null) {
                        addChip(dietaryChipGroup, preferenceValue);
                    }
                }
            }
        }
    }

    private void addChip(ChipGroup chipGroup, String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setClickable(false);
        chip.setCheckable(false);
        chipGroup.addView(chip);
    }

    @SuppressLint("MissingInflatedId")
    private void showEditDialog(String title, String currentValue, String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_field, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextInputEditText editText = dialogView.findViewById(R.id.edit_text);

        dialogTitle.setText("Edit " + title);
        editText.setText(currentValue);
        editText.setHint("Enter your " + title.toLowerCase());

        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newValue = editText.getText().toString().trim();
                    if (!newValue.isEmpty()) {
                        updateField(field, newValue);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateField(String field, String value) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase.child("users").child(user.getUid()).child(field).setValue(value)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Profile.this,
                                    "Updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Profile.this,
                                    "Failed to update", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}