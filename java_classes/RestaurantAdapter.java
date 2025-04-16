package com.example.foodbot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
    private List<Restaurant> restaurantList;
    private Context context;
    private OnRestaurantClickListener listener;

    // Interface for click handling
    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    public RestaurantAdapter(Context context, OnRestaurantClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.restaurantList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        holder.name.setText(restaurant.getName());
        holder.foodType.setText(restaurant.getFoodType());
        holder.location.setText(restaurant.getLocation());

        // Set rating
        float rating = restaurant.getAverageRating();
        holder.ratingBar.setRating(rating);
        holder.ratingText.setText(String.format("%.1f (%d)", rating, restaurant.getTotalRatings()));

        // Load image using Glide
        Glide.with(context)
                .load(restaurant.getImageUrl())
                .into(holder.image);

        // Set click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestaurantClick(restaurant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurantList = restaurants;
        notifyDataSetChanged();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, foodType, location, ratingText;
        RatingBar ratingBar;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.restaurant_image);
            name = itemView.findViewById(R.id.restaurant_name);
            foodType = itemView.findViewById(R.id.food_type);
            location = itemView.findViewById(R.id.location);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            ratingText = itemView.findViewById(R.id.rating_text);
        }
    }
}