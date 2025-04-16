package com.example.foodbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EmailVerification extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView userEmailText;
    private Button resendButton, checkButton, backButton;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        userEmailText = findViewById(R.id.user_email);
        resendButton = findViewById(R.id.resend_button);
        checkButton = findViewById(R.id.check_button);
        backButton = findViewById(R.id.back_button);

        // Get data from intent
        userEmail = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        userEmailText.setText(userEmail);

        // Create account and send verification
        createAccount(userEmail, password);

        // Setup button clicks
        resendButton.setOnClickListener(v -> resendVerificationEmail());
        checkButton.setOnClickListener(v -> checkVerificationStatus());
        backButton.setOnClickListener(v -> {
            deleteAccount();
        });
    }

    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Log the error
                                            String error = emailTask.getException() != null ?
                                                    emailTask.getException().getMessage() : "Unknown error";
                                            Toast.makeText(this,
                                                    "Failed to send verification email: " + error,
                                                    Toast.LENGTH_LONG).show();
                                            user.delete(); // Delete the created user since email verification failed
                                            startActivity(new Intent(this, Authentication.class));
                                            finish();
                                        }
                                    });
                        }
                    } else {
                        // Log the specific error
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this,
                                "Account creation failed: " + error,
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, Authentication.class));
                        finish();
                    }
                });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> reloadTask) {
                    if (reloadTask.isSuccessful()) {
                        user.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> emailTask) {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(EmailVerification.this,
                                                    "Verification email sent!",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(EmailVerification.this,
                                                    "Failed to send verification email: " +
                                                            emailTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(EmailVerification.this,
                                "Failed to refresh user status. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(EmailVerification.this,
                    "No user found. Please sign up again.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void checkVerificationStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        saveUserData(user.getUid());  // Save user data first
                    } else {
                        Toast.makeText(EmailVerification.this,
                                "Please verify your email first",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveUserData(String userId) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", getIntent().getStringExtra("name"));
        userUpdates.put("email", getIntent().getStringExtra("email"));
        userUpdates.put("phone", getIntent().getStringExtra("phone"));

        mDatabase.child("users").child(userId).updateChildren(userUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(EmailVerification.this, Preferences.class));
                        finish();
                    } else {
                        Toast.makeText(EmailVerification.this,
                                "Failed to save user data",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mAuth.signOut();

            mDatabase.child("users").child(user.getUid()).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EmailVerification.this,
                                                        "Account deleted successfully",
                                                        Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(EmailVerification.this,
                                                        Authentication.class));
                                                finish();
                                            } else {
                                                Toast.makeText(EmailVerification.this,
                                                        "Failed to delete account completely: " +
                                                                task.getException().getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    });
        }
    }
}