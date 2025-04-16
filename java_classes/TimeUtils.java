package com.example.foodbot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getRelativeTimeSpan(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // Convert to seconds
        long seconds = diff / 1000;
        if (seconds < 60) {
            return "Just now";
        }

        // Convert to minutes
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minutes ago";
        }

        // Convert to hours
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hours ago";
        }

        // Convert to days
        long days = hours / 24;
        if (days < 7) {
            return days + " days ago";
        }

        // Use regular date format for older dates
        return formatTimestamp(timestamp);
    }
}