package com.example.citylinkrentals.model;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import android.widget.TextView;

public class TimeUtil {

    public static void setRelativeTime(TextView timeText, Property property) {
        String relativeTime = "N/A";

        if (property.getCreatedAt() != null) {
            try {
                String createdAtStr = property.getCreatedAt();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

                ZoneId indiaZone = ZoneId.of("Asia/Kolkata");

                LocalDateTime createdAtLocal = LocalDateTime.parse(createdAtStr, formatter);
                ZonedDateTime createdAt = createdAtLocal.atZone(indiaZone);
                ZonedDateTime now = ZonedDateTime.now(indiaZone);

                Duration duration = Duration.between(createdAt, now);

                long totalMinutes = duration.toMinutes();

                long weeks = totalMinutes / (7 * 24 * 60);
                long days = (totalMinutes % (7 * 24 * 60)) / (24 * 60);
                long hours = (totalMinutes % (24 * 60)) / 60;
                long minutes = totalMinutes % 60;

                StringBuilder sb = new StringBuilder();

                if (weeks > 0) sb.append(weeks).append("w ");
                if (days > 0) sb.append(days).append("d ");
                if (hours > 0) sb.append(hours).append("h ");
                if (minutes > 0) sb.append(minutes).append("m ");

                if (sb.length() > 0) {
                    relativeTime = sb.toString().trim() + " ago";
                } else {
                    relativeTime = "just now";
                }
            } catch (Exception e) {
                e.printStackTrace();
                relativeTime = "Invalid date";
            }
        }
        timeText.setText(relativeTime);
    }
}
