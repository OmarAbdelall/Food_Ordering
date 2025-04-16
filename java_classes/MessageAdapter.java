package com.example.foodbot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private Context context;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getType() == Message.TYPE_USER) {
            holder.userMessageLayout.setVisibility(View.VISIBLE);
            holder.botMessageLayout.setVisibility(View.GONE);
            holder.userMessageText.setText(message.getMessage());
        } else {
            holder.userMessageLayout.setVisibility(View.GONE);
            holder.botMessageLayout.setVisibility(View.VISIBLE);
            holder.botMessageText.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout userMessageLayout, botMessageLayout;
        TextView userMessageText, botMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessageLayout = itemView.findViewById(R.id.user_message_layout);
            botMessageLayout = itemView.findViewById(R.id.bot_message_layout);
            userMessageText = itemView.findViewById(R.id.user_message_text);
            botMessageText = itemView.findViewById(R.id.bot_message_text);
        }
    }
}