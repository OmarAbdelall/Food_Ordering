package com.example.foodbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Authentication extends AppCompatActivity {
    private EditText nameInput, emailInput, phoneInput, passwordInput;
    private Button signupButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        nameInput = findViewById(R.id.Name_Input);
        emailInput = findViewById(R.id.Email_Input);
        phoneInput = findViewById(R.id.Phone_Input);
        passwordInput = findViewById(R.id.editTextTextPassword);
        signupButton = findViewById(R.id.signup_button);
        TextView loginTextView = findViewById(R.id.login_textview);

        // Setup password toggle


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    signUpUser();
                }
            }
        });

        // Navigation to Sign In
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Authentication.this, SIgnIn.class));
                finish();
            }
        });
    }

    private boolean validateInputs() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            return false;
        }

        if (phone.isEmpty()) {
            phoneInput.setError("Phone number is required");
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void signUpUser() {
        // Only collect and validate info, don't create account yet
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // First check if email exists
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SignInMethodQueryResult result = task.getResult();
                        if (result != null && result.getSignInMethods() != null &&
                                result.getSignInMethods().isEmpty()) {
                            // Email not in use, proceed to verification
                            Intent intent = new Intent(Authentication.this, EmailVerification.class);
                            intent.putExtra("email", email);
                            intent.putExtra("password", password);
                            intent.putExtra("name", name);
                            intent.putExtra("phone", phone);
                            startActivity(intent);
                            finish();
                        } else {
                            emailInput.setError("Email already in use");
                            Toast.makeText(Authentication.this,
                                    "This email is already registered",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(Authentication.this,
                                "Error checking email: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(String userId) {
        UserModel user = new UserModel(
                nameInput.getText().toString().trim(),
                emailInput.getText().toString().trim(),
                phoneInput.getText().toString().trim()
        );

        mDatabase.child("users").child(userId).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Authentication.this,
                                    "Registration successful!",
                                    Toast.LENGTH_SHORT).show();
                            // Remove this navigation to Preferences
                            // startActivity(new Intent(Authentication.this, Preferences.class));
                            // finish();
                        } else {
                            Toast.makeText(Authentication.this,
                                    "Failed to save user data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}