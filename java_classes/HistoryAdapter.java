package com.example.foodbot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<OrderHistoryItem> historyItems;
    private Context context;

    public HistoryAdapter(Context context) {
        this.context = context;
        this.historyItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        OrderHistoryItem item = historyItems.get(position);

        holder.restaurantName.setText(item.getRestaurantName());
        holder.mealName.setText(item.getMealName());
        holder.price.setText(String.format("$%.2f", item.getPrice()));

        // Use TimeUtils for timestamp display
        String timeText = TimeUtils.getRelativeTimeSpan(item.getTimestamp());
        holder.timestamp.setText(timeText);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public void setItems(List<OrderHistoryItem> items) {
        this.historyItems = new ArrayList<>(items); // Create a new list to avoid reference issues

        // Sort items by timestamp (newest first)
        Collections.sort(this.historyItems, (item1, item2) ->
                Long.compare(item2.getTimestamp(), item1.getTimestamp())
        );

        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantName, mealName, price, timestamp;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            mealName = itemView.findViewById(R.id.meal_name);
            price = itemView.findViewById(R.id.price);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }

    // Helper method to add a single item
    public void addItem(OrderHistoryItem item) {
        historyItems.add(0, item); // Add to the beginning of the list
        notifyItemInserted(0);
    }

    // Helper method to clear all items
    public void clearItems() {
        int size = historyItems.size();
        historyItems.clear();
        notifyItemRangeRemoved(0, size);
    }
}