package com.example.foodbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import pl.droidsonroids.gif.GifImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Logo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        // Check if user is already signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User is signed in and verified, go directly to Restaurants
            startActivity(new Intent(Logo.this, Restaurants.class));
            finish();
            return;
        }

        // Initialize views
        GifImageView chatbotGif = findViewById(R.id.chatbot_gif);
        TextView appNameText = findViewById(R.id.app_name_text);
        TextView taglineText = findViewById(R.id.tagline_text);
        MaterialButton logoButton = findViewById(R.id.logo_button);

        // Set initial alpha to 0 (invisible)
        chatbotGif.setAlpha(0f);
        appNameText.setAlpha(0f);
        taglineText.setAlpha(0f);
        logoButton.setAlpha(0f);

        // Create fade-in animations
        chatbotGif.animate()
                .alpha(1f)
                .setDuration(1000);

        appNameText.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(300);

        taglineText.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(600);

        logoButton.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(900);

        // Button click handler
        logoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Logo.this, Authentication.class);
            startActivity(intent);
            finish();
        });
    }
}