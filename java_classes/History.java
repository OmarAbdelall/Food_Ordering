package com.example.foodbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
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
import java.util.Collections;
import java.util.List;

public class History extends AppCompatActivity {
    private RecyclerView historyRecyclerView;
    private TextView emptyHistoryText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        historyRecyclerView = findViewById(R.id.history_recycler_view);
        emptyHistoryText = findViewById(R.id.empty_history_text);
        ImageButton backButton = findViewById(R.id.back_arrow);

        // Setup RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(this);
        historyRecyclerView.setAdapter(historyAdapter);

        // Setup back button
        backButton.setOnClickListener(v -> finish());

        // Load order history
        loadOrderHistory();
    }

    private void loadOrderHistory() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference historyRef = mDatabase.child("users").child(userId)
                .child("stats").child("orderHistory");

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OrderHistoryItem> historyItems = new ArrayList<>();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    OrderHistoryItem item = orderSnapshot.getValue(OrderHistoryItem.class);
                    if (item != null) {
                        historyItems.add(item);
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(historyItems, (o1, o2) ->
                        Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                if (historyItems.isEmpty()) {
                    emptyHistoryText.setVisibility(View.VISIBLE);
                    historyRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyHistoryText.setVisibility(View.GONE);
                    historyRecyclerView.setVisibility(View.VISIBLE);
                    historyAdapter.setItems(historyItems);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(History.this,
                        "Failed to load history: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}