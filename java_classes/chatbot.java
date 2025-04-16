package com.example.foodbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class chatbot extends AppCompatActivity {
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private FloatingActionButton sendButton;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;
    // Replace with your computer's IP address when testing
    private static final String API_URL = "";  // This points to localhost on the Android emulator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialize views
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        ImageButton backArrow = findViewById(R.id.back_arrow);

        // Setup RecyclerView
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(this, messageList);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Back button click
        backArrow.setOnClickListener(v -> finish());

        // Send button click
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText("");
            }
        });
    }

    private void sendMessage(String message) {
        // Add user message to chat
        messageList.add(new Message(message, Message.TYPE_USER));
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        messagesRecyclerView.scrollToPosition(messageList.size() - 1);

        // Create JSON request
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Make API call
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, jsonRequest,
                response -> {
                    try {
                        String botResponse = response.getString("response");
                        messageList.add(new Message(botResponse, Message.TYPE_BOT));
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(chatbot.this,
                                "Error processing response",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(chatbot.this,
                            "Error connecting to chatbot: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonObjectRequest);
    }
}